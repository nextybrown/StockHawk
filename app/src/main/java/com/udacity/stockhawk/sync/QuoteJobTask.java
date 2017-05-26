package com.udacity.stockhawk.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

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

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

/**
 * Created by nestorkokoafantchao on 5/9/17.
 */

public class QuoteJobTask {

    private static final int YEARS_OF_HISTORY = 2;
    private static Map<String, Stock> quotes = new HashMap<String, Stock>();



    synchronized  public  static  void getQuote(Context context){
        Timber.d("Running sync job");

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0){
                return;
            }

            quotes = YahooFinance.get(stockArray);
            Timber.d(quotes.toString());


            ArrayList<ContentValues> dataToContentValeur = quoteDataToContentValeur(stockCopy);

            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.delete(Contract.Quote.URI,null,null);


            contentResolver
                    .bulkInsert(
                            Contract.Quote.URI,
                            dataToContentValeur.toArray(new ContentValues[dataToContentValeur.size()]));
        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }

    }


    private static ArrayList<ContentValues> quoteDataToContentValeur( Set<String> stockCopy) throws IOException {
        Iterator<String> iterator = stockCopy.iterator();
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);
        ArrayList<ContentValues> quoteCVs = new ArrayList<>();

        while (iterator.hasNext()) {

            String symbol = iterator.next();
            Stock stock = quotes.get(symbol);
            StockQuote quote = stock.getQuote();
            float price = quote.getPrice().floatValue();
            float change = quote.getChange().floatValue();
            float percentChange = quote.getChangeInPercent().floatValue();

            // WARNING! Don't request historical data for a stock that doesn't exist!
            // The request will hang forever X_x
            List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);
            StringBuilder historyBuilder = new StringBuilder();

            for (HistoricalQuote it : history) {
                historyBuilder.append(it.getDate().getTimeInMillis());
                historyBuilder.append(", ");
                historyBuilder.append(it.getClose());
                historyBuilder.append("\n");
            }
            ContentValues quoteCV = new ContentValues();
            quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
            quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
            quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
            quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
            quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());

            quoteCVs.add(quoteCV);
        }
        return quoteCVs;
    }





}
