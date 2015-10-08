package com.ppgsalomao.findorcreatesample.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ppgsalomao.findorcreatesample.R;
import com.ppgsalomao.findorcreatesample.persistence.User;
import com.ppgsalomao.findorcreatesample.persistence.strategy.DatabaseSyncStrategy;
import com.ppgsalomao.findorcreatesample.persistence.strategy.FindOrCreateDatabaseSyncStrategy;
import com.ppgsalomao.findorcreatesample.util.JSONUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity
        implements DatabaseSyncStrategy.DatabaseSyncDelegate<User, User> {

    @Bind(R.id.main_user_list)
    protected ListView userListView;
    @Bind(R.id.main_log_list)
    protected ListView logListView;

    protected ArrayList<String> logMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.loadUserList();

        this.logMessages = new ArrayList<>();
        this.updateLogAdapter();
    }

    private void loadUserList() {
        Realm realm = Realm.getInstance(this);

        RealmResults<User> results = realm.where(User.class).findAllSorted("id");
        UserAdapter adapter = new UserAdapter(this, results, true);
        this.userListView.setAdapter(adapter);
    }

    private void updateLogAdapter() {
        this.logListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_adapter_log_view, this.logMessages));
    }

    private void addLog(String message) {
        this.logMessages.add(message);
        this.updateLogAdapter();
    }

    @OnClick(R.id.main_load_list_1)
    public void onLoadList1ButtonClicked() {
        try {
            String listRaw = new JSONUtil().getJSONString(this, R.raw.user_list_1);
            this.insertJsonOnDatabase(listRaw);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.main_load_list_2)
    public void onLoadList2ButtonClicked() {
        try {
            String listRaw = new JSONUtil().getJSONString(this, R.raw.user_list_2);
            this.insertJsonOnDatabase(listRaw);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.main_clear_log)
    public void onClearLogClicked() {
        this.logMessages = new ArrayList<>();
        this.updateLogAdapter();
    }

    private void insertJsonOnDatabase(String json) {
        this.onClearLogClicked();

        Realm realm = Realm.getInstance(this);

        List<User> persistedUsers = new ArrayList<>();
        persistedUsers.addAll(realm.allObjects(User.class));
        List<User> users = new Gson().fromJson(json, new TypeToken<List<User>>(){}.getType());

        DatabaseSyncStrategy.NumericalIdExtractor<User> numericalIdExtractor =
                new DatabaseSyncStrategy.NumericalIdExtractor<User>() {
                    @Override
                    public int getNumericalId(User user) {
                        return user.getId();
                    }
                };

        final DatabaseSyncStrategy<User, User> databaseSyncStrategy =
                new FindOrCreateDatabaseSyncStrategy<>(this, numericalIdExtractor, numericalIdExtractor);

        databaseSyncStrategy.setNewObjectsList(users);
        databaseSyncStrategy.setPersistedObjectsList(persistedUsers);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                databaseSyncStrategy.updateDatabase();
            }
        });
    }

    /* DatabaseSyncStrategy.DatabaseSyncDelegate<User, User> */

    @Override
    public void save(User newUser, User persistedUser) {
        if(persistedUser != null) {
            this.addLog(String.format("Atualizou usuário [%d] %s", persistedUser.getId(), persistedUser.getName()));
            persistedUser.setName(newUser.getName());
        } else {
            this.addLog(String.format("Adicionou usuário [%d] %s", newUser.getId(), newUser.getName()));
            Realm realm = Realm.getInstance(this);
            realm.copyToRealm(newUser);
        }
    }

    @Override
    public void delete(User persistedUser) {
        this.addLog(String.format("Removeu usuário [%d] %s", persistedUser.getId(), persistedUser.getName()));
        persistedUser.removeFromRealm();
    }
}
