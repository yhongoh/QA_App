package jp.techacademy.hongou.yuka.qa_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
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


public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private int mGenre = 0;
    private int mFavorite;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private DatabaseReference favoriteRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;
    private ArrayList<FavoriteQuestion> mFavoriteQuestionArrayList;
    private FavoriteQuestionListAdapter mFavoriteAdapter;
    private Question mQuestion;


    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d("Main_onChildAdded", String.valueOf(dataSnapshot));
            HashMap map = (HashMap) dataSnapshot.getValue();
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

            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
            mQuestionArrayList.add(question);
            mAdapter.notifyDataSetChanged();
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
            Log.d("Main_onChildAdded_fav", String.valueOf(dataSnapshot));

            HashMap map = (HashMap) dataSnapshot.getValue();
            String title = (String) map.get("Title");
            String body = (String) map.get("Body");
            String name = (String) map.get("Name");
            String uid = (String) map.get("Uid");
            String imageString = (String) map.get("Image");
            byte[] bytes;
            if (imageString != null) {
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            FavoriteQuestion favoriteQuestion = new FavoriteQuestion(title, body, name,uid, dataSnapshot.getKey(), bytes);
            mFavoriteQuestionArrayList.add(favoriteQuestion);
            mFavoriteAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            mFavoriteAdapter.notifyDataSetChanged();
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
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ジャンルを選択していない場合（mGenre == 0）はエラーを表示する
                if (mGenre == 0){
                    Snackbar.make(view, "ジャンルを選択してください", Snackbar.LENGTH_LONG).show();
                }

                //ログイン済みのユーザを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                //ログインしていなければログイン画面に遷移する
                if (user == null) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    //ジャンルを渡して質問作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);
                    Log.d("Main_genre", String.valueOf(mGenre));
                    startActivity(intent);
                    }
            }
        });

    //ナビゲーションドロワーの設定
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
        //未ログイン状態の場合、お気に入りを非表示にする
        Menu menu = navigationView.getMenu();
        MenuItem favItem = menu.findItem(R.id.nav_favorite);
        favItem.setVisible(false);
        }


    navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
        @Override
        public boolean onNavigationItemSelected (MenuItem item){
        int id = item.getItemId();

        if (id == R.id.nav_hobby) {
            mToolbar.setTitle("趣味");
            mGenre = 1;
        }else if (id == R.id.nav_life) {
            mToolbar.setTitle("生活");
            mGenre = 2;
        } else if (id == R.id.nav_health) {
            mToolbar.setTitle("健康");
            mGenre = 3;
        } else if (id == R.id.nav_computer) {
            mToolbar.setTitle("コンピューター");
            mGenre = 4;
        } else if (id == R.id.nav_favorite) {
                mToolbar.setTitle("お気に入り");
                mGenre = 5;}


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


            if (mGenre == 5) {
                //お気に入り質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mQuestionArrayList.clear();
                Log.d("clear2", "clear2");
                mAdapter.setQuestionArrayList(mQuestionArrayList);
                mListView.setAdapter(mAdapter);
            } else {
                //質問リストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mQuestionArrayList.clear();
                Log.d("clear1", "clear1");
                mAdapter.setQuestionArrayList(mQuestionArrayList);
                mListView.setAdapter(mAdapter);
            }

            //選択したジャンルにリスナーを登録する
            if (mGenreRef != null) {
                mGenreRef.removeEventListener(mEventListener);
            }

            if (mGenre != 5){
            mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
            Log.d("Main_mGenreRef", String.valueOf(mGenreRef));
            mGenreRef.addChildEventListener(mEventListener);
            return true;}
            else {
                favoriteRef = mDatabaseReference.child(Const.FavoritePATH);
                Log.d("Main_favoriteRef", String.valueOf(favoriteRef));
                favoriteRef.addChildEventListener(mFavoriteListener);
                return true;
            }
        }
    });

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();
        Log.d("mAdapter", "mAdapter");

            /*
            mListView = (ListView) findViewById(R.id.listView);
            mFavoriteAdapter = new FavoriteQuestionListAdapter(this);
            mFavoriteQuestionArrayList = new ArrayList<FavoriteQuestion>();
            mFavoriteAdapter.notifyDataSetChanged();
            Log.d("mFavoriteAdapter", "mFavoriteAdapter");
            */

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {
                if (mGenre == 5) {
                    // お気に入り一覧画面を起動する
                    Intent intent = new Intent(getApplicationContext(), FavoriteActivity.class);
                    intent.putExtra("question", mQuestionArrayList.get(postion));
                    Log.d("main_position", String.valueOf(mQuestionArrayList.get(postion)));
                    startActivity(intent);
                } else {
                    // Questionのインスタンスを渡して質問詳細画面を起動する
                    Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                    intent.putExtra("question", mQuestionArrayList.get(postion));
                    startActivity(intent);
                }
            }
        });

}

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "onResume");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        if (user == null) {
            //未ログイン状態の場合、お気に入りを非表示にする
            Menu menu = navigationView.getMenu();
            MenuItem favItem = menu.findItem(R.id.nav_favorite);
            favItem.setVisible(false);
        } else {
            Menu menu = navigationView.getMenu();
            MenuItem favItem = menu.findItem(R.id.nav_favorite);
            favItem.setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
        }
    }
