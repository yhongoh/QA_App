package jp.techacademy.hongou.yuka.qa_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FavoriteActivity extends AppCompatActivity {
    private int mGenre = 0;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private DatabaseReference mFavGenreRef;
    private DatabaseReference mContentsRef;
    private DatabaseReference favoriteRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;
    private HashMap fav_data;
    private String favId;
    private String favGenre;
    private String genre;
    private String mQuestionUid;
    private ArrayList<String> mFavoriteQuestionUidList;


    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            // ジャンルごとにまず取れるので、それを１つずつ見て中身を抽出する
            HashMap<String, HashMap<String, String>> map = (HashMap) dataSnapshot.getValue();
            for (Map.Entry<String, HashMap<String, String>> questionData : map.entrySet()) {
                Question question = convertMapToQuestion(questionData.getKey(), questionData.getValue());

               if (mFavoriteQuestionUidList.contains(question.getQuestionUid())) {
                   mQuestionArrayList.add(question);
                   Log.d("FA_mQAL", String.valueOf(question));
               }
            }
        }

        // HashMapのデータをQuestionオブジェクトに変換する
        private Question convertMapToQuestion(String questionUid, HashMap map) {
            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String imageString = (String) map.get("image");
            byte[] bytes;
            if (imageString != null) {
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                for (Object key : answerMap.keySet()) {
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }
            return new Question(title, body, name, uid, questionUid, mGenre, bytes, answerArrayList);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            //変更があったQuestionを探す
            for (Question question : mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    //このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mFavoriteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
           HashMap<String, String> fav_data = new HashMap<String, String>();
            fav_data.put("favId",String.valueOf(dataSnapshot.getKey()));
            fav_data.put("favGenre", String.valueOf(dataSnapshot.child("genre").getValue()));
            List<String> mFavoriteQuestionUidList = new ArrayList<>(fav_data.values());
            Log.d("mFavQUL", String.valueOf(mFavoriteQuestionUidList));
            }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        setTitle("お気に入り");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        favoriteRef = mDatabaseReference.child(Const.FavoritePATH).child(user.getUid());
        favoriteRef.addChildEventListener(mFavoriteListener);

        mFavGenreRef = mDatabaseReference.child(Const.ContentsPATH);
        mFavGenreRef.addChildEventListener(mEventListener);

        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        //mAdapter.notifyDataSetChanged();
        Log.d("fav_mAdapter", "mAdapter");
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(postion));
                startActivity(intent);
            }
        });

    }
}
