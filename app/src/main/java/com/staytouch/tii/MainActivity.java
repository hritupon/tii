package com.staytouch.tii;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.staytouch.tii.api.ApiClient;
import com.staytouch.tii.api.ApiInterface;
import com.staytouch.tii.models.Article;
import com.staytouch.tii.models.News;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    public static final String API_KEY = "36f077c83c66451990b2111804419bd1";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private TextView topHeadLine;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout errorLayout;
    private ImageView errorImage;
    private TextView errorTitle, errorMessage;
    private Button btnRetry;
    private Adapter adapter;
    private FloatingActionButton plusFloatingActionButton;
    private FloatingActionButton broadcastFloatingActionButton;
    private FloatingActionButton addQueryFloatingActionButton;
    Animation FabOpen, FabClose, FabClockwise, FabAntiClockwise;
    boolean isFabOpen =false;

    private String TAG = MainActivity.class.getSimpleName();
    public static final long DAY_IN_MS = 1000 * 60 * 60 * 24;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        topHeadLine = findViewById(R.id.topHeadlines);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        onLoadingSwipeRefresh("");

        errorLayout = findViewById(R.id.errorLayout);
        errorImage = findViewById(R.id.errorImage);
        errorTitle = findViewById(R.id.errorTitle);
        errorMessage = findViewById(R.id.errorMessage);
        btnRetry = findViewById(R.id.btnRery);

        FabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        FabClockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise);
        FabAntiClockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anticlockwise);


        plusFloatingActionButton = findViewById(R.id.add);
        broadcastFloatingActionButton = findViewById(R.id.broadcast);
        addQueryFloatingActionButton = findViewById(R.id.addQuery);


        plusFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFabOpen){
                    broadcastFloatingActionButton.startAnimation(FabClose);
                    addQueryFloatingActionButton.startAnimation(FabClose);
                    plusFloatingActionButton.startAnimation(FabAntiClockwise);
                    broadcastFloatingActionButton.setClickable(false);
                    addQueryFloatingActionButton.setClickable(false);
                    isFabOpen=false;
                }else{
                    broadcastFloatingActionButton.startAnimation(FabOpen);
                    addQueryFloatingActionButton.startAnimation(FabOpen);
                    plusFloatingActionButton.startAnimation(FabClockwise);
                    broadcastFloatingActionButton.setClickable(true);
                    addQueryFloatingActionButton.setClickable(true);
                    isFabOpen=true;
                }
            }
        });

        broadcastFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plusFloatingActionButton.callOnClick();
                startActivity(new Intent(MainActivity.this, AddNewsActivity.class));
            }
        });

        addQueryFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plusFloatingActionButton.callOnClick();
                startActivity(new Intent(MainActivity.this, QueryActivity.class));
            }
        });


    }

    public void LoadJson(final String keyword) {
        errorLayout.setVisibility(View.GONE);
        topHeadLine.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String country = Utils.getCountry();
        String language = Utils.getLanguage();
        Call<News> call;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");

        Date toDate = Calendar.getInstance().getTime();
        String toDateStr = dateFormat.format(toDate);

        Date fromDate = new Date(toDate.getTime() - (7 * DAY_IN_MS));
        String fromDateStr = dateFormat.format(fromDate);

        if(keyword!=null && keyword.trim().length()>0){
            call = apiInterface.getNewsSearch(keyword, language,"publishedAt", API_KEY);

        }else{
            call = apiInterface.getNews(country,/* toDateStr,fromDateStr, */ API_KEY);

        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if(response.isSuccessful() && response.body().getArticles()!= null){
                    if(!articles.isEmpty()){
                        articles.clear();
                    }
                    articles = response.body().getArticles();
                    adapter = new Adapter(articles, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListener();

                    topHeadLine.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false );
                }else{
                    topHeadLine.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false );
                    String errorCode;
                    switch (response.code()){
                        case 404:
                            errorCode = "404 not found";
                            break;
                        case 500:
                            errorCode = "500 server broken";
                            break;
                        default:
                            errorCode= "Unknown error";
                            break;
                    }
                    showErrorMessage(R.drawable.no_result, "No Result", "Please Try Again!\n"+ errorCode );
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                topHeadLine.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false );
                String errorCode = t.getMessage();
                showErrorMessage(R.drawable.no_result, "Oops", "Network failure, Please Try Again!\n"+ errorCode );

            }
        });

    }

    private void initListener(){
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                ImageView imageView = view.findViewById(R.id.img);
                Intent intent = new Intent(MainActivity.this, NewsDetailActivity.class);

                Article article = articles.get(position);
                intent.putExtra("url", article.getUrl());
                intent.putExtra("title", article.getTitle());
                intent.putExtra("img", article.getUrlToImage());
                intent.putExtra("date", article.getPublishedAt());
                intent.putExtra("source", article.getSource().getName());
                intent.putExtra("author", article.getAuthor());

                Pair<View, String> pair = Pair.create((View)imageView, ViewCompat.getTransitionName(imageView));
                ActivityOptionsCompat  optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, pair);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                    startActivity(intent, optionsCompat.toBundle());
                }else{
                    startActivity(intent);
                }

            }
        });
    }

    @Override
   public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search Latest News...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String keyword) {
                if(keyword.length() > 2){
                    onLoadingSwipeRefresh(keyword);
                } else {
                    Toast.makeText(MainActivity.this, "Type more than two letters!", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String keyword) {
                //onLoadingSwipeRefresh(keyword);
                return false;
            }
        });
        searchMenuItem.getIcon().setVisible(false,false);

        return true;

    }

    @Override
    public void onRefresh() {
        LoadJson(" ");
    }

    private void onLoadingSwipeRefresh(final String keyword) {
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        LoadJson(keyword);
                    }
                }
        );
    }

    private void showErrorMessage(int imageView, String title, String message){
        if(errorLayout.getVisibility()==View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
        }
        errorImage.setImageResource(imageView);
        errorTitle.setText(title);
        errorMessage.setText(message);

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoadingSwipeRefresh("");
            }
        });
    }
}
