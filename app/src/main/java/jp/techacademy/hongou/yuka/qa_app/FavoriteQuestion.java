package jp.techacademy.hongou.yuka.qa_app;

import java.io.Serializable;
import java.util.ArrayList;

public class FavoriteQuestion implements Serializable {
    private String mTitle;
    private String mBody;
    private String mName;
    private String mUid;
    private String mQuestionUid;
    private int mGenre;
    private byte[] mBitmapArray;


    /*
    public String getTitle(){
        return mTitle;
    }

    public String getBody(){
        return mBody;
    }

    public String getName(){
        return mName;
    }
    */

    public String getUid(){
        return mUid;
    }

    public String getQuestionUid(){
        return mQuestionUid;
    }

    public int getGenre(){
        return mGenre;
    }

    /*
    public byte[] getImageBytes(){
        return mBitmapArray;
    }


    public ArrayList<Answer> getAnswers(){
        return mAnswerArrayList;
    }
    */

    public FavoriteQuestion(String questionUid, int genre) {
        //mUid = uid;
        mQuestionUid = questionUid;
        mGenre = genre;
    }
}