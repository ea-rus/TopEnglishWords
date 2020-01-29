package earus.com.topenglishwords;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Learn.DB_PATH="/storage/emulated/0/_my/top3k.db";
        Learn.DB_PATH2="/storage/emulated/0/_my/dictEN-RU.db";

        setContentView(R.layout.activity_main);
        Button button_learn = (Button) findViewById(R.id.button_learn);
        Button button_new = (Button) findViewById(R.id.button_new);
        Button button_unknown = (Button) findViewById(R.id.button_unknown);
        Button button_stat = (Button) findViewById(R.id.button_stat);
        Button button_top_use = (Button) findViewById(R.id.button_top_use);
        Button button_lasts_export = (Button) findViewById(R.id.button_lasts_export);

        button_new.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToLearn("NEW");
                    }
                }
        );

        button_unknown.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToLearn("UNKNOWN");
                    }
                }
        );
        button_learn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToLearn("LEARN");
                    }
                }
        );
        button_top_use.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToLearn("TOP_USE");
                    }
                }
        );

        button_lasts_export.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SQLiteDatabase db = SQLiteDatabase.openDatabase(Learn.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                        String[] args = {};
                        Cursor cursor = db.rawQuery("select eng, state, count, DATE(lastdate) as lastdate from words where lastdate > date('now',  '-3 day') order by DATE(lastdate) desc, state, eng", args);

                        String out="";
                        cursor.moveToFirst();

                        String cur_lastdate="";
                        String lastdate="";
                        for (int i=0; i<cursor.getCount(); i++){

                            out+= cursor.getString(cursor.getColumnIndex("eng"));
                            out+= " : ";
                            out+= cursor.getString(cursor.getColumnIndex("state"));
                            out+= " ";
                            out+= cursor.getString(cursor.getColumnIndex("count"));
                            out+= "\n";

                            lastdate = cursor.getString(cursor.getColumnIndex("lastdate"));
                            if (!cur_lastdate.equals(lastdate)){
                                if (!cur_lastdate.equals("")) out+= "\n";
                                cur_lastdate = lastdate;
                            }
                            cursor.moveToNext();
                        }
                        cursor.close();
                        db.close();

//                        Toast toast = Toast.makeText(getApplicationContext(),
//                                out, Toast.LENGTH_SHORT);
//                        toast.show();
//                        return;

                        if (out.equals("")){
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "No words", Toast.LENGTH_SHORT);
                            toast.show();
                        }else {
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, out);
                            sendIntent.setType("text/plain");

                            Intent shareIntent = Intent.createChooser(sendIntent, null);
                            startActivity(shareIntent);
                        }

                    }
                }
        );


        button_stat.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SQLiteDatabase db = SQLiteDatabase.openDatabase(Learn.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                        String[] args = {};
                        Cursor cursor = db.rawQuery("select state, count(*) count from words group by state", args);

                        String out="";
                        cursor.moveToFirst();
                        for (int i=0; i<cursor.getCount(); i++){
                            out+= cursor.getString(cursor.getColumnIndex("state"));
                            out+= ": ";
                            out+= cursor.getString(cursor.getColumnIndex("count"));
                            out+= "\n";
                            cursor.moveToNext();
                         }
                        Toast toast = Toast.makeText(getApplicationContext(),
                                out, Toast.LENGTH_SHORT);
                        toast.show();
                        cursor.close();
                        db.close();
                    }
                }
        );


    }



    public void goToLearn(String wordType){
        //Log.d("db", wordType);
        Intent intent = new Intent(MainActivity.this, Learn.class);
        intent.putExtra("type", wordType);
        startActivity(intent);

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
