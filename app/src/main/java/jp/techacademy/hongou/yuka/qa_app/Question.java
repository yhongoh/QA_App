package jp.techacademy.hongou.yuka.qa_app;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by hongoyuka on 2018/01/31.
 */

public class Question implements Serializable {
    private String mTitle;
    private String mBody;
    private String mName;
    private String mUid;
    private String mQuestionUid;
    private int mGenre;
    private byte[] mBitmapArray;
    private ArrayList<Answer> mAnswerArrayList;

    public String getTitle(){
        return mTitle;
    }

    public String getBody(){
        return mBody;
    }

    public String getName(){
        return mName;
    }

    public String getUid(){
        return mUid;
    }

    public String getQuestionUid(){
        return mQuestionUid;
    }

    public int getGenre(){
        return mGenre;
    }

    public byte[] getImageBytes(){
        return mBitmapArray;
    }


    public ArrayList<Answer> getAnswers(){
        return mAnswerArrayList;
    }


    public Question(String title, String body, String name, String uid, String questionUid, int genre, byte[] bytes,ArrayList<Answer> answers) {
        mTitle = title;
        mBody = body;
        mName = name;
        mUid = uid;
        mQuestionUid = questionUid;
        mGenre = genre;
        mBitmapArray = bytes.clone();
        mAnswerArrayList = answers;
        Log.d("Ques_title", String.valueOf(mTitle));
        Log.d("Ques_body", String.valueOf(mBody));
        Log.d("Ques_name", String.valueOf(mName));
        Log.d("Ques_uid", String.valueOf(mUid));
        Log.d("Ques_qUid", String.valueOf(mQuestionUid));
    }
}
