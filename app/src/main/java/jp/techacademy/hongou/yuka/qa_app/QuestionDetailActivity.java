package jp.techacademy.hongou.yuka.qa_app;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    boolean mIsFavorite;


    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d("QDA_onChildAdded_event",String.valueOf(dataSnapshot));
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for (Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
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

        private ChildEventListener mFavoriteListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    mIsFavorite = true;
                    Log.d("QDA_mIsFavorite3", String.valueOf(mIsFavorite));

                    Button mFavoriteButton = (Button) findViewById(R.id.favoriteButton);
                    mFavoriteButton.setText("お気に入りを解除");
                    mFavoriteButton.setBackgroundColor(Color.GRAY);
                    mFavoriteButton.setVisibility(View.VISIBLE);
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
        setContentView(R.layout.activity_question_detail);
        Log.d("QDA_onCreate", "onCreate");

        //Questionクラスのインスタンスを保持し、タイトルを設定
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");
        Log.d("QDA_question", String.valueOf(mQuestion));
        setTitle(mQuestion.getTitle());

        //ListViewの設定
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        //ログイン済みのユーザーを取得する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference favoriteRef = databaseReference.child(Const.FavoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());
        favoriteRef.addChildEventListener(mFavoriteListener);

        if (user == null) {
            Button mFavoriteButton = (Button) findViewById(R.id.favoriteButton);
            mFavoriteButton.setVisibility(View.INVISIBLE);}
        else {
            Button mFavoriteButton = (Button) findViewById(R.id.favoriteButton);
            if (mIsFavorite) {
                mFavoriteButton.setText("お気に入りを解除");
                mFavoriteButton.setBackgroundColor(Color.GRAY);
                mFavoriteButton.setVisibility(View.VISIBLE);
                mFavoriteButton.setOnClickListener(this);
            } else {
                mFavoriteButton.setText("お気に入りに追加");
                int color = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
                mFavoriteButton.setBackgroundColor(color);
                mFavoriteButton.setVisibility(View.VISIBLE);
                mFavoriteButton.setOnClickListener(this);
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Log.d("QDA_onClick", "onClick");
        //ログイン済みのユーザーを取得する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        } else {
            if (view.getId() == R.id.favoriteButton) {
                //Firebaseにお気に入り質問UIDを登録
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                DatabaseReference favoriteRef = databaseReference.child(Const.FavoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                //favoriteRef.addChildEventListener(mFavoriteListener);

                Map<String, String> favorite = new HashMap<String, String>();
                favorite.put("genre", String.valueOf(mQuestion.getGenre()));

                Log.d("QDA_mIsFavorite", String.valueOf(mIsFavorite));

            if (mIsFavorite) {
                favoriteRef.setValue(null);
                Button mFavoriteButton = (Button) findViewById(R.id.favoriteButton);
                mFavoriteButton.setText("お気に入りに追加");
                int color = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
                mFavoriteButton.setBackgroundColor(color);
                mIsFavorite = false;
                Log.d("QDA_mIsFavorite2",String.valueOf(mIsFavorite));

            } else {
                favoriteRef.setValue(favorite);
                Button mFavoriteButton = (Button) findViewById(R.id.favoriteButton);
                mFavoriteButton.setText("お気に入りを解除");
                mFavoriteButton.setBackgroundColor(Color.GRAY);
                mIsFavorite = true;
                Log.d("QDA_mIsFavorite4",String.valueOf(mIsFavorite));
            }
            } else {
                // Questionを渡して回答作成画面を起動する
                Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                intent.putExtra("question", mQuestion);
                startActivity(intent);
            }
        }
    }
}
