package com.rd.demo;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.rd.demo.fixtures.FixturesData;
import com.rd.demo.fixtures.MessagesFixtures;
import com.rd.demo.model.DaoSession;
import com.rd.demo.model.Message;
import com.rd.demo.model.MsgBean;
import com.rd.demo.model.MsgBeanDao;
import com.rd.demo.model.User;
import com.rd.demo.utils.AppUtils;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.greenrobot.greendao.database.Database;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/*
 * Created by troy379 on 04.04.17.
 */
public abstract class DemoMessagesActivity extends AppCompatActivity
        implements MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener {

    private static final int TOTAL_MESSAGES_COUNT = 100;

    protected final String senderId = "0";
    protected ImageLoader imageLoader;
    protected MessagesListAdapter<Message> messagesAdapter;

    private Menu menu;
    private int selectionCount;
    private Date lastLoadedDate;

    //
    private MsgBeanDao msgBeanDao;
    // from是距离现在更近的时间 to 是距离现在更久远的时间
    private long fromStamp;
    private long toStamp = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                Picasso.with(DemoMessagesActivity.this).load(url).into(imageView);
            }
        };

        //
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        msgBeanDao = daoSession.getMsgBeanDao();
        //
        fromStamp = Calendar.getInstance().getTime().getTime();

    }


    @Override
    protected void onStart() {
        super.onStart();
        //messagesAdapter.addToStart(MessagesFixtures.getTextMessage(), true);
        queryAll();
        Log.i("hehe", "11111111111111111111111111111111111111111111111111111111");
        List<Message> messages = queryAllParts();
        messagesAdapter.addToEnd(messages, true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.chat_actions_menu, menu);
        onSelectionChanged(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                messagesAdapter.deleteSelectedMessages();
                break;
            case R.id.action_copy:
                messagesAdapter.copySelectedMessagesText(this, getMessageStringFormatter(), true);
                AppUtils.showToast(this, R.string.copied_message, true);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed();
        } else {
            messagesAdapter.unselectAllItems();
        }
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Log.i("hehe", "onLoadMore: " + page + " " + totalItemsCount);
        //if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
        loadMessages();
        //}
    }

    @Override
    public void onSelectionChanged(int count) {
        this.selectionCount = count;
        menu.findItem(R.id.action_delete).setVisible(count > 0);
        menu.findItem(R.id.action_copy).setVisible(count > 0);
    }

    protected void loadMessages() {
        new Handler().postDelayed(new Runnable() { //imitation of internet connection
            @Override
            public void run() {
                Log.i("hehe", " load message");
                /*ArrayList<Message> messages = MessagesFixtures.getMessages(lastLoadedDate);
                lastLoadedDate = messages.get(messages.size() - 1).getCreatedAt();
                messagesAdapter.addToEnd(messages, false);*/

                List<Message> messages = queryAllParts();
                messagesAdapter.addToEnd(messages, true);

            }
        }, 1000);
    }

    private MessagesListAdapter.Formatter<Message> getMessageStringFormatter() {
        return new MessagesListAdapter.Formatter<Message>() {
            @Override
            public String format(Message message) {
                String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                        .format(message.getCreatedAt());

                String text = message.getText();
                if (text == null) text = "[attachment]";

                return String.format(Locale.getDefault(), "%s: %s (%s)",
                        message.getUser().getName(), text, createdAt);
            }
        };
    }

    protected void addMsg(String content, String username) {
        MsgBean msgBean = new MsgBean();
        msgBean.setContent(content);
        msgBean.setUsername(username);
        msgBean.setTimeStamp(Calendar.getInstance().getTime().getTime());
        msgBeanDao.insert(msgBean);
    }

    protected Message getClientMessage(String text) {
        return new Message(FixturesData.getRandomId(), getClient(), text);
    }

    protected Message getServerMessage(String text) {
        return new Message(FixturesData.getRandomId(), getServer(), text);
    }

    private User getClient() {
        return new User("0", "client", "http://pic49.nipic.com/file/20140929/9448607_180233775000_2.jpg", true);
    }

    private User getServer() {
        return new User("1", "server", "http://pic16.nipic.com/20110917/7125440_175819145000_2.jpg", true);
    }

    protected List<Message> queryAll() {
        List<MsgBean> msgBeans = msgBeanDao.queryBuilder().list();
        List<Message> messages = new ArrayList<>();
        for (MsgBean msgBean : msgBeans) {
            String id = msgBean.getId().toString();
            String content = msgBean.getContent();
            long timpStamp = msgBean.getTimeStamp();
            User user = msgBean.getUsername().equals("client") ? getClient() : getServer();
            Date date = new Date();
            date.setTime(timpStamp);
            Message message = new Message(id, user, content, date);
            messages.add(message);
            Date date1 = new Date();
            date1.setTime(msgBean.getTimeStamp());
            Log.i("hehe", " " + dateFormate(date1) + " " + content);
        }
        return messages;
    }

    // from  是距离现在更近的时间，to是更久远的时间
    protected List<Message> queryAllParts() {
        //拿过去七天的数据
        if (toStamp == -1)
            toStamp = beforSomeDay(fromStamp, 7);
        List<MsgBean> msgBeans = msgBeanDao.queryRaw("where TIME_STAMP <= ? and TIME_STAMP > ? order by TIME_STAMP ASC", new String[]{Long.toString(fromStamp), Long.toString(toStamp)});
        if (msgBeans == null || msgBeans.size() < 1) {
            //说明过去七天没达到数据，拿过去三十天的数据试试
            fromStamp = toStamp;
            toStamp = beforSomeDay(fromStamp, 30);
            msgBeans = msgBeanDao.queryRaw("where TIME_STAMP <= ? and TIME_STAMP > ? order by TIME_STAMP ASC", new String[]{Long.toString(fromStamp), Long.toString(toStamp)});
            if (msgBeans == null || msgBeans.size() < 1) {
                //说明过去 30 天也没数据 那就拿过去 180天的数据
                fromStamp = toStamp;
                toStamp = beforSomeDay(fromStamp, 180);
                msgBeans = msgBeanDao.queryRaw("where TIME_STAMP <= ? and TIME_STAMP > ? order by TIME_STAMP ASC", new String[]{Long.toString(fromStamp), Long.toString(toStamp)});
                if (msgBeans == null || msgBeans.size() < 1) {
                    //说明过去 180 天也没数据 那就拿过去 360天的数据
                    fromStamp = toStamp;
                    toStamp = beforSomeDay(fromStamp, 360);
                    msgBeans = msgBeanDao.queryRaw("where TIME_STAMP <= ? and TIME_STAMP > ? order by TIME_STAMP ASC", new String[]{Long.toString(fromStamp), Long.toString(toStamp)});
                    if (msgBeans == null || msgBeans.size() < 1) {
                        //说明过去 360 天也没数据 那就拿过去 3600天的数据
                        fromStamp = toStamp;
                        toStamp = beforSomeDay(fromStamp, 3600);
                        msgBeans = msgBeanDao.queryRaw("where TIME_STAMP <= ? and TIME_STAMP > ? order by TIME_STAMP ASC", new String[]{Long.toString(fromStamp), Long.toString(toStamp)});
                        if (msgBeans == null || msgBeans.size() < 1) {
                            Toast.makeText(this, "过去几年都没有聊天记录了", Toast.LENGTH_LONG).show();
                        } else {
                            fromStamp = toStamp;
                            toStamp = beforSomeDay(fromStamp, 7);
                        }
                    } else {
                        fromStamp = toStamp;
                        toStamp = beforSomeDay(fromStamp, 7);
                    }
                } else {
                    fromStamp = toStamp;
                    toStamp = beforSomeDay(fromStamp, 7);
                }
            } else {
                fromStamp = toStamp;
                toStamp = beforSomeDay(fromStamp, 7);
            }

        } else {
            fromStamp = toStamp;
            toStamp = beforSomeDay(fromStamp, 7);
        }
        List<Message> messages = new ArrayList<>();
        for (MsgBean msgBean : msgBeans) {
            String id = msgBean.getId().toString();
            String content = msgBean.getContent();
            User user = msgBean.getUsername().equals("client") ? getClient() : getServer();
            Date date = new Date();
            date.setTime(msgBean.getTimeStamp());
            Message message = new Message(id, user, content, date);
            messages.add(message);
            Log.i("hehe", " " + dateFormate(date));
        }
        return messages;
    }

    private String dateFormate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = simpleDateFormat.format(date.getTime());//
        return time;
    }

    //根据传入的 时间戳 计算出 某些天以前的时间戳 是啥
    private long beforSomeDay(long timeStamp, int daysize) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        calendar.add(Calendar.DAY_OF_YEAR, -daysize);
        return calendar.getTimeInMillis();
    }


}
