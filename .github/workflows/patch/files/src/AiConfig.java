package com.{pkg}.ai;
import android.content.Context;
import org.json.JSONObject;
import java.io.InputStream;
public class AiConfig {
public final String baseUrl, language, authHeader;
public AiConfig(String baseUrl, String language, String authHeader) {
this.baseUrl = baseUrl; this.language = language; this.authHeader = authHeader;
}
public static AiConfig load(Context ctx) {
try (InputStream is = ctx.getAssets().open(“ai_config.json”)) {
byte[] b = new byte[is.available()]; is.read(b);
JSONObject o = new JSONObject(new String(b));
return new AiConfig(o.optString(“baseUrl”,“”), o.optString(“language”,“ru”), o.optString(“authHeader”,“”));
} catch (Exception e) { return new AiConfig(“”, “ru”, “”); }
}
}
