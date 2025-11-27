package com.{pkg}.ai;
import java.io.; import java.net.; import javax.net.ssl.HttpsURLConnection;
public class SseClient {
public interface TokenHandler { void onToken(String t); }
public static void ask(String baseUrl, String lang, String authHeader, String message, String historyJson, TokenHandler handler) throws Exception {
if (baseUrl==null || baseUrl.isEmpty()) {
// офлайн демо
handler.onToken(“Демо-режим: сервер ИИ не настроен. “); handler.onToken(“Опишите симптом, код ошибки или контекст, “);
handler.onToken(“а затем подключите сервер через assets/ai_config.json (baseUrl).”); return;
}
URL url = new URL(baseUrl.replaceAll(”/+$”,””) + “/ai/ask”);
HttpURLConnection c = (HttpURLConnection) url.openConnection();
c.setRequestMethod(“POST”);
c.setRequestProperty(“Accept”,“text/event-stream”);
c.setRequestProperty(“Content-Type”,“application/json; charset=utf-8”);
if (authHeader != null && !authHeader.isEmpty()) c.setRequestProperty(“Authorization”, authHeader);
c.setDoOutput(true); c.setConnectTimeout(15000); c.setReadTimeout(0);
String body = “{"message":”+json(message)+“,"history":”+(historyJson==null?“[]”:historyJson)+“,"lang":”+json(lang)+“}”;
try(OutputStream os = c.getOutputStream()) { os.write(body.getBytes(“UTF-8”)); }
try (BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream(), “UTF-8”))) {
String line; while ((line = br.readLine()) != null) if (line.startsWith(“data: “)) handler.onToken(line.substring(6));
}
}
private static String json(String s){ if (s==null) return “null”; return “"” + s.replace(”\”,“\\”).replace(“"”,“\"”).replace(“\n”,“\n”) + “"”; }
}
