package todo.list.warmup.act;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;
import todo.list.warmup.R;
import todo.list.warmup.adpt.ToDoListAdapter;
import todo.list.warmup.api.RestList;
import todo.list.warmup.bean.ToDoList;
import todo.list.warmup.dia.Dialog;
import todo.list.warmup.utils.Utils;
import todo.list.warmup.view.DividerItemDecoration;
import todo.list.warmup.view.Snack;

import static todo.list.warmup.R.id.fab;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private ToDoListAdapter adapter;
    private NavigationView navigationView;

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvUID;
    private ImageView imAvatar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);

        bindViews();
        customConfiguration();
        bindListeners();
        setup();
        singInSetup();

     /*   for (int i = 1; i <= 5; i++) {
            adapter.add(new ToDoList("List Example " + i));
        }*/
    }

    private void bindViews() {
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        this.recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        this.drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.navigationView = (NavigationView) findViewById(R.id.nav_view);

        this.tvName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.name);
        this.tvEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.email);
        this.tvUID = (TextView) navigationView.getHeaderView(0).findViewById(R.id.uid);
        this.imAvatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.avatar);
    }

    private void customConfiguration() {
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, 0, 0);

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        setTitle(R.string.main_title);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        navigationView.setCheckedItem(R.id.action_home);
    }

    private void bindListeners() {
        floatingActionButton.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setup() {
        this.adapter = new ToDoListAdapter(this);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = recyclerView.getChildLayoutPosition(view);

                Intent i = new Intent(MainActivity.this, ItemActivity.class);
                i.putExtra(ItemActivity.SELECTED_LIST_ID, adapter.getItem(position).getId());
                startActivity(i);
            }
        });

        adapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final int position = recyclerView.getChildLayoutPosition(view);

                final MaterialDialog dialog = new MaterialDialog(MainActivity.this)
                        .setTitle(R.string.delete).setCanceledOnTouchOutside(true)
                        .setMessage(R.string.want_delete);

                dialog.setPositiveButton(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        adapter.remove(position);

                        RestList.delete(adapter.getItem(position).getId());
                    }
                }).setNegativeButton(R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;
            }
        });
    }


    private void singInSetup() {
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case fab:
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setTitle(R.string.add).setMessage(R.string.want_new_list);
                dialog.setEdit(getString(R.string.list_hint), null);
                dialog.setPositiveButton(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        RestList.newList(dialog.getEditText().getText().toString(), new RestList.IOnNewOne() {
                            @Override
                            public void onNewOne(ToDoList list) {
                                adapter.add(list);
                                Snack.show(floatingActionButton, R.string.list_created);
                            }
                        });
                    }
                });
                dialog.setNegativeButton(R.string.cancel).show();
                break;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }


    @Override
    public void onStart() {
        super.onStart();

        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return onDrawerMenuSelected(item.getItemId());
    }

    private boolean onDrawerMenuSelected(int resId) {
        switch (resId) {
            case R.id.action_home:
                break;

            case R.id.action_sign_out:
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setTitle(R.string.sign_out).setMessage(R.string.want_sign_out);
                dialog.setPositiveButton(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        auth.signOut();
                        startActivity(new Intent(MainActivity.this, SignInActivity.class));
                        finish();
                    }
                });
                dialog.setNegativeButton(R.string.cancel).show();

                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
            FirebaseUser user = auth.getCurrentUser();

            if (user != null) {


                tvName.setText(Utils.coalesce(user.getDisplayName(), getString(R.string.unknow)));
                tvEmail.setText(Utils.coalesce(user.getEmail(), getString(R.string.unknow)));
                tvUID.setText(user.getUid());


                Transformation transformation = new RoundedTransformationBuilder()
                        .borderColor(ContextCompat.getColor(MainActivity.this, R.color.freeze))
                        .borderWidthDp(3)
                        .cornerRadiusDp(32)
                        .oval(false)
                        .build();

                Picasso.with(MainActivity.this).load(user.getPhotoUrl()).fit()
                        .transform(transformation).into(imAvatar);

                // Snack.show(floatingActionButton, getString(R.string.uid_message, FirebaseAuth.getInstance().getCurrentUser().getUid()));

                //Refresh Lists
                RestList.getAll(new RestList.IOnGetAll() {
                    @Override
                    public void onGetAll(List<ToDoList> list) {
                        adapter.addAll(list);
                    }
                });
            }
        }
    };

}
