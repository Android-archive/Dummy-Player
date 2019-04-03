package com.amsavarthan.ztunes;

import android.app.Application;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class RecentsViewModel extends AndroidViewModel {

    private RecentsDao recentsDao;
    private ExecutorService executorService;

    public RecentsViewModel(@NonNull Application application) {
        super(application);
        recentsDao = RecentsDatabase.getInstance(application).recentsDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    LiveData<List<RecentsEntity>> findAllRecents() {
        return recentsDao.findAll();
    }

    List<RecentsEntity> getAllRecents() {
        return recentsDao.getAll();
    }

    RecentsEntity findSongEntityByName(String name){
        return recentsDao.findByName(name);
    }

    String findSongByName(String name){
        try {
            return recentsDao.findByName(name).getName();
        }catch (Exception e){
            return "null";
        }
    }

    int getRecentsCount(){
        return recentsDao.countRecents();
    }

    void savePost(final RecentsEntity recentsEntity) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                recentsDao.save(recentsEntity);
            }
        });
    }

    void deletePost(final RecentsEntity recentsEntity) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                recentsDao.delete(recentsEntity);
            }
        });
    }
}