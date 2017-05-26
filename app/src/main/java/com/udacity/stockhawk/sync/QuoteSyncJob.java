package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int SYNC_INTERVAL_HOURS = 10;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS/10;
    private static final int ONE_OFF_ID = 2;
    private static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static boolean isInitialized=false;
    private static final String MOVIE_JOB_TAG="quote_job_tag";

    private QuoteSyncJob() {
    }

    static void getQuotes(Context context) {



    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");
        Driver  driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);
        Job job = jobDispatcher.newJobBuilder()
                .setTag(MOVIE_JOB_TAG)
                .setService(QuoteJobService.class)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setTrigger(Trigger.executionWindow(SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setReplaceCurrent(true)
                .build();
    }


    public static synchronized void initialize(@NonNull final Context context) {

        if(isInitialized)
            return;

         isInitialized= true;
        schedulePeriodic(context);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                Cursor cursor = context.getContentResolver().query(Contract.Quote.URI,
                        null, null, null, null);

                if(cursor== null || cursor.getCount()==0){
                    syncImmediately(context);
                }
            }
        });
        thread.start();


    }

    public static synchronized void syncImmediately(@NonNull final Context context) {
        Intent intent = new Intent(context,QuoteIntentService.class);
        context.startService(intent);
    }


}
