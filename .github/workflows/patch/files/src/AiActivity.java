package com.{pkg}.ai;
import android.app.Activity; import android.os.; import android.view.; import android.widget.*;
public class AiActivity extends Activity {
LinearLayout chat; EditText input; Button send; TextView status; AiConfig cfg;
@Override protected void onCreate(Bundle b) {
super.onCreate(b);
int layoutId = getResources().getIdentifier(“ai_activity”,“layout”,getPackageName());
setContentView(layoutId);
setTitle(getString(getResources().getIdentifier(“ai_title”,“string”,getPackageName())));
chat = findViewById(getId(“chat”)); input = findViewById(getId(“input”));
send = findViewById(getId(“send”)); status = findViewById(getId(“status”));
cfg = AiConfig.load(this);
appendSystem(getString(getResources().getIdentifier(“ai_disclaimer”,“string”,getPackageName())));
send.setOnClickListener(v -> { String q = input.getText().toString().trim(); if (!q.isEmpty()) { appendUser(q); input.setText(“”); askAsync(q); } });
}
private int getId(String name){ return getResources().getIdentifier(name,“id”,getPackageName()); }
private void appendUser(String t){ append(“Вы”, t); }
private void appendAssistant(String t){ append(“ИИ”, t); }
private void appendSystem(String t){ append(“Система”, t); }
private void append(String who, String t){ TextView tv=new TextView(this); tv.setTextIsSelectable(true); tv.setText(who + “: " + t); tv.setPadding(0, dp(6), 0, dp(6)); chat.addView(tv); scrollToEnd(); }
private int dp(int v){ return (int)(getResources().getDisplayMetrics().density * v); }
private void scrollToEnd(){ ScrollView sv = findViewById(getId(“scroll”)); sv.post(() -> sv.fullScroll(View.FOCUS_DOWN)); }
private void askAsync(String q){
status.setText(“Генерация…”);
new Thread(() -> {
StringBuilder acc = new StringBuilder();
try {
SseClient.ask(cfg.baseUrl, “ru”, cfg.authHeader, q, “[]”, token -> runOnUiThread(() -> {
acc.append(token);
if (chat.getChildCount()==0 || !(((TextView)chat.getChildAt(chat.getChildCount()-1)).getText()+”“).startsWith(“ИИ:”)) appendAssistant(”“);
TextView last = (TextView)chat.getChildAt(chat.getChildCount()-1); last.setText(“ИИ: " + acc.toString());
}));
runOnUiThread(() -> status.setText(””));
} catch (Exception e) {
runOnUiThread(() -> { status.setText("Ошибка: " + e.getMessage()); appendSystem(“Сервер недоступен.”); });
}
}).start();
}
}
