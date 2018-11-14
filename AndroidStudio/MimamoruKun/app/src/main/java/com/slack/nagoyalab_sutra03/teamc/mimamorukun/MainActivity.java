package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends Activity implements OnClickListener {

    private List<TextView> textViewList;
    private List<EventHistory> eventHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewList = new ArrayList<>();
        textViewList.add((TextView)findViewById(R.id.text_history1));
        textViewList.add((TextView)findViewById(R.id.text_history2));
        textViewList.add((TextView)findViewById(R.id.text_history3));
        textViewList.add((TextView)findViewById(R.id.text_history4));
        textViewList.add((TextView)findViewById(R.id.text_history5));
        textViewList.add((TextView)findViewById(R.id.text_history6));

        for(int i=0; i<textViewList.size(); i++){
            textViewList.get(i).setOnClickListener(this);
        }

        //イベント履歴を取得
        getEventHistory();

        //イベント履歴を表示
        displayEventHistory();
    }

    //ボタンクリック時の関数
    @Override
    public void onClick(View v) {

        for(int i=0; i<textViewList.size(); i++){
            if(v == textViewList.get(i)){
                Intent intent = new Intent(this, EventDetailActivity.class);

                EventHistory history = eventHistoryList.get(i);
                intent.putExtra("DATE", history.getOccuredDate().getDate());
                intent.putExtra("NAME", history.getEventName());
                intent.putExtra("CONTENT", history.getEventContent());

                startActivityForResult(intent, 0);
            }
        }
    }

    /*
    イベント履歴取得メソッドの仮実装
     */
    private void getEventHistory(){
        this.eventHistoryList = new ArrayList<>();

        //1件目
        EventHistory history = new EventHistory();
        history.setOccuredDate(new GregorianCalendar(2018, 11 - 1, 11, 13, 40, 10).getTime());
        history.setEventName("振るイベント対応");
        history.setEventContents("対応コメント: トイレに付き添いました。");
        this.eventHistoryList.add(history);

        //2件目
        history = new EventHistory();
        history.setOccuredDate(new GregorianCalendar(2018, 11 - 1, 11, 13, 30, 25).getTime());
        history.setEventName("振るイベント発生");
        history.setEventContents("振るイベントが発生しました。");
        this.eventHistoryList.add(history);

        //3件目
        history = new EventHistory();
        history.setOccuredDate(new GregorianCalendar(2018, 11 - 1, 11, 7, 1, 53).getTime());
        history.setEventName("起床イベント対応");
        history.setEventContents("対応コメント: 確認しました。");
        this.eventHistoryList.add(history);

        //4件目
        history = new EventHistory();
        history.setOccuredDate(new GregorianCalendar(2018, 11 - 1, 11, 7, 0, 3).getTime());
        history.setEventName("起床イベント発生");
        history.setEventContents("起床イベントが発生しました。");
        this.eventHistoryList.add(history);

        //5件目
        history = new EventHistory();
        history.setOccuredDate(new GregorianCalendar(2018, 11 - 1, 10, 23, 45, 43).getTime());
        history.setEventName("温度イベント対応");
        history.setEventContents("室温が29.4℃(30℃以下)に下がりました。");
        this.eventHistoryList.add(history);

        //6件目
        history = new EventHistory();
        history.setOccuredDate(new GregorianCalendar(2018, 11 - 1, 10, 23, 40, 32).getTime());
        history.setEventName("温度イベント発生");
        history.setEventContents("室温が32.5℃(30℃以上)に上がりました。");
        this.eventHistoryList.add(history);
    }

    /*
    イベント履歴表示メソッドの仮実装
     */
    private void displayEventHistory(){

        DateFormat sdf= new SimpleDateFormat("MM/dd");

        for(int i=0; i<6; i++){
            if(eventHistoryList.size()>i){
                textViewList.get(i).setText(
                        sdf.format(eventHistoryList.get(i).getOccuredDate()) +
                        eventHistoryList.get(i).getEventName());
            }
        }
    }

}
