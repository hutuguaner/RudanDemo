package com.rd.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.rd.demo.fixtures.MessagesFixtures;
import com.rd.demo.model.Message;
import com.rd.demo.tencentai.Sign;
import com.rd.demo.utils.AppUtils;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class MainActivity extends DemoMessagesActivity
        implements MessageInput.InputListener,
        MessageInput.AttachmentsListener,
        MessageInput.TypingListener, RecognizerDialogListener {


    public static void open(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    private MessagesList messagesList;

    //语音识别
    private RecognizerDialog recognizerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_my);
        initPermissionDo();

        this.messagesList = findViewById(R.id.messagesList);
        initAdapter();

        MessageInput input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(this);
        input.setTypingListener(this);
        input.setAttachmentsListener(this);

        recognizerDialog = new RecognizerDialog(this, new InitListener() {
            @Override
            public void onInit(int i) {

            }
        });
        recognizerDialog.setListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void initPermissionDo() {
        if (Build.VERSION.SDK_INT >= 23) {
            initPermission();
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.CAMERA
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        for (int i = 0; i < permissions.length; i++) {
        }
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != 0) {
                //说明 权限申请 被拒绝
                Toast.makeText(MainActivity.this, "请全部接受权限申请，否则将无法使用", Toast.LENGTH_LONG).show();
                MainActivity.this.finish();
                return;
            }
        }
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        Log.i("hehe", "" + input);
        super.messagesAdapter.addToStart(
                getClientMessage(input.toString()), true);
        addMsg(input.toString(), "client");
        tencentAiChat(input.toString());
        return true;
    }

    @Override
    public void onAddAttachments() {
        /*super.messagesAdapter.addToStart(
                MessagesFixtures.getImageMessage(), true);*/
        recognizerDialog.show();
    }

    private void initAdapter() {
        super.messagesAdapter = new MessagesListAdapter<>(super.senderId, super.imageLoader);
        super.messagesAdapter.enableSelectionMode(this);
        super.messagesAdapter.setLoadMoreListener(this);
        super.messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
                    @Override
                    public void onMessageViewClick(View view, Message message) {
                        AppUtils.showToast(MainActivity.this,
                                message.getUser().getName() + " avatar click",
                                false);
                    }
                });
        this.messagesList.setAdapter(super.messagesAdapter);
    }

    @Override
    public void onStartTyping() {
        Log.v("Typing listener", getString(R.string.start_typing_status));
    }

    @Override
    public void onStopTyping() {
        Log.v("Typing listener", getString(R.string.stop_typing_status));
    }


    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        Log.i("hehe", " onResult : " + recognizerResult.getResultString());
        try {
            JSONObject jsonObject = new JSONObject(recognizerResult.getResultString());
            JSONArray wsJsonArray = jsonObject.getJSONArray("ws");
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < wsJsonArray.length(); i++) {
                JSONObject item = wsJsonArray.getJSONObject(i);
                JSONArray cwJsonArray = item.getJSONArray("cw");
                for (int j = 0; j < cwJsonArray.length(); j++) {
                    JSONObject cwItem = cwJsonArray.getJSONObject(j);
                    String w = cwItem.getString("w");
                    if (w != null && !w.equals("")) {
                        stringBuffer.append(w);
                    }
                }

            }
            String content = stringBuffer.toString();
            if (content != null && !content.trim().equals("")) {
                super.messagesAdapter.addToStart(
                        getClientMessage(content), true);
                addMsg(content, "client");
                tencentAiChat(content);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(SpeechError speechError) {
        Log.i("hehe", " onError : " + speechError.getErrorDescription());
        Toast.makeText(MainActivity.this, speechError.getErrorDescription(), Toast.LENGTH_LONG).show();
        recognizerDialog.dismiss();
    }


    //tencent ai chat
    private void tencentAiChat(String msg) {
        Map<String, Object> hehe = null;
        try {
            hehe = Sign.getSignature(Sign.getKeybyvalue(msg), Const.app_key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (hehe == null || hehe.isEmpty()) {
            return;
        }

        OkGo.<String>get(Const.tencentAiChat).params("app_id", (String) hehe.get("app_id")).params("time_stamp", (Long) hehe.get("time_stamp")).params("nonce_str", (String) hehe.get("nonce_str")).params("sign", (String) hehe.get("sign")).params("session", (String) hehe.get("session")).params("question", (String) hehe.get("question")).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                Log.i("hehe", " onSuccess : " + response.body());
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    int ret = jsonObject.getInt("ret");
                    if (ret == 0) {
                        // success
                        JSONObject data = jsonObject.getJSONObject("data");
                        String answer = data.getString("answer");
                        MainActivity.super.messagesAdapter.addToStart(getServerMessage(answer), true);
                        addMsg(answer, "server");
                    } else {
                        // failure

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
