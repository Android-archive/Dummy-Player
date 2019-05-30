package com.amsavarthan.ztunes.room;

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

    public LiveData<List<RecentsEntity>> findAllRecents() {
        return recentsDao.findAll();
    }

    public List<RecentsEntity> getAllRecents() {
        return recentsDao.getAll();
    }

    public RecentsEntity findSongEntityByName(String name){
        return recentsDao.findByName(name);
    }

    public String findSongByName(String name){
        try {
            return recentsDao.findByName(name).getName();
        }catch (Exception e){
            return "null";
        }
    }

    public int getRecentsCount(){
        return recentsDao.countRecents();
    }

    public void savePost(final RecentsEntity recentsEntity) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                recentsDao.save(recentsEntity);
            }
        });
    }

    public void deletePost(final RecentsEntity recentsEntity) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                recentsDao.delete(recentsEntity);
            }
        });
    }
}