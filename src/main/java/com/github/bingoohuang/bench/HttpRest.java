package com.github.bingoohuang.bench;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@Slf4j
public class HttpRest {
  public static final HttpClient client = HttpClientBuilder.create().build();

  private final RequestConfig requestConfig =
      RequestConfig.custom()
          // 从连接池获取到连接的超时时间，如果是非连接池的话，该参数暂时没有发现有什么用处
          .setConnectionRequestTimeout(30 * 1000)
          // 指客户端和服务进行数据交互的时间，是指两者之间如果两个数据包之间的时间大于该时间则认为超时，而不是整个交互的整体时间，
          // 比如如果设置1秒超时，如果每隔0.8秒传输一次数据，传输10次，总共8秒，这样是不超时的。
          // 而如果任意两个数据包之间的时间超过了1秒，则超时。
          .setSocketTimeout(30 * 1000)
          // 建立连接的超时时间
          .setConnectTimeout(30 * 1000)
          .build();

  /**
   * GET请求。
   *
   * @param url 请求地址。
   * @return 响应结果。
   */
  @SneakyThrows
  public String get(String url) {
    HttpGet request = new HttpGet(url);
    request.setConfig(requestConfig);

    HttpResponse response = client.execute(request);
    return getSuccessBody(request, response);
  }

  /**
   * GET请求。
   *
   * @param url 请求地址。
   * @return 响应结果。
   */
  @SneakyThrows
  public String post(String url, Map<String, String> params) {
    HttpPost request = new HttpPost(url);
    request.setConfig(requestConfig);
    // 组织请求参数
    ArrayList<NameValuePair> paramList = new ArrayList<>();
    if (params != null && params.size() > 0) {
      Set<String> keySet = params.keySet();
      for (String key : keySet) {
        paramList.add(new BasicNameValuePair(key, params.get(key)));
      }
    }

    request.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));

    HttpResponse response = client.execute(request);
    return getSuccessBody(request, response);
  }

  /**
   * 删除请求。
   *
   * @param url 请求地址。
   * @return 响应结果。
   */
  @SneakyThrows
  public String delete(String url) {
    HttpDelete request = new HttpDelete(url);
    request.setConfig(requestConfig);

    HttpResponse response = client.execute(request);
    return getSuccessBody(request, response);
  }

  /**
   * 下载文件请求。
   *
   * @param url 请求地址。
   * @param os 文件存储实现。
   */
  @SneakyThrows
  public void download(String url, OutputStream os) {
    HttpGet request = new HttpGet(url);
    request.setConfig(requestConfig);

    HttpResponse response = client.execute(request);
    int code = response.getStatusLine().getStatusCode();
    if (code >= 200 && code < 300) {
      response.getEntity().writeTo(os);
      return;
    }

    String body = EntityUtils.toString(response.getEntity());

    throw new RuntimeException(
        "url ["
            + url
            + "] failed code:["
            + code
            + "] body:["
            + body
            + "] headers:["
            + Arrays.toString(response.getAllHeaders())
            + "]");
  }

  /**
   * 上传文件。
   *
   * @param url 请求地址。
   * @param fileName 上传文件名。
   * @param file 文件内容输入流。
   * @return 响应结果。
   */
  @SneakyThrows
  public String upload(String url, String fileName, InputStream file) {
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    builder.addBinaryBody(fileName, file, ContentType.DEFAULT_BINARY, fileName);

    HttpPost request = new HttpPost(url);
    request.setConfig(requestConfig);
    request.setEntity(builder.build());

    HttpResponse response = client.execute(request);

    return getSuccessBody(request, response);
  }

  @SneakyThrows
  private static String getSuccessBody(HttpRequestBase request, HttpResponse response) {
    int code = response.getStatusLine().getStatusCode();
    String body = EntityUtils.toString(response.getEntity());
    String uri = request.getURI().toString();
    log.info("{} {}, code:{}, body:{}", request.getMethod(), uri, code, body);
    if (code >= 200 && code < 300) {
      return body;
    }

    throw new RuntimeException(
        "url ["
            + uri
            + "] failed code:["
            + code
            + "] body:["
            + body
            + "] headers:["
            + Arrays.toString(response.getAllHeaders())
            + "]");
  }
}
