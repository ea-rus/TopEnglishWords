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
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
* status
*   N новый
*   Y известный
*   U неизвестный
*   T подучить
*   A добавлено, запрашивалось
* */

public class Learn extends Activity {



    static Button button_yes;
    static Button button_no;
    static Button button_maybe;

    static TextView textView_eng;
    static TextView textView_rus;
    static TextView textView_rating;
    static TextView   textView_wordCount;

    static Integer wordId;
    static Integer wordCount;

    String wordType;

    static String DB_PATH;
    static String DB_PATH2;

    static String currentMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        wordType = getIntent().getExtras().getString("type");


        button_yes = (Button) findViewById(R.id.button_yes);
        button_no = (Button) findViewById(R.id.button_no);
        button_maybe = (Button) findViewById(R.id.button_maybe);

        textView_eng = (TextView) findViewById(R.id.textView_eng);
        textView_rus = (TextView) findViewById(R.id.textView_rus);
        textView_rating = (TextView) findViewById(R.id.textView_rating);
        textView_wordCount = (TextView) findViewById(R.id.textView_wordCount);
        Button button_descr = (Button) findViewById(R.id.button_descr);

        setMode("First");

        button_yes.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appLogic("Yes");
                    }
                }
        );

        button_no.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appLogic("No");
                    }
                }
        );

        button_maybe.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       appLogic("Maybe");
                    }
                }
        );

        button_descr.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String word=textView_eng.getText().toString();
                        SQLiteDatabase db;
                        try {
                            db = SQLiteDatabase.openDatabase(DB_PATH2, null, SQLiteDatabase.OPEN_READONLY);
                        }catch (SQLiteException e){
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "base not found :" + DB_PATH2, Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }

                        String[] args = {};
                        Cursor cursor = db.rawQuery("select caseword, /*substr(descr,0,800)*/ descr from dictionary where word = '"+word+"' ", args);

                        String out = "";
                        cursor.moveToFirst();
                        for (int i = 0; i < cursor.getCount(); i++) {
                            out += cursor.getString(cursor.getColumnIndex("caseword"));
                            out += ":<br> ";
                            out += cursor.getString(cursor.getColumnIndex("descr"));
                            out += "<br>";
                            cursor.moveToNext();
                        }
                        cursor.close();
                        db.close();

                        if (out==""){
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Translate not found: "+word, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else {
                            alert(out);
                        }


                    }
                }
        );
    }
    public void alert(String text) {
        new AlertDialog.Builder(this).setMessage (Html.fromHtml(text)).setNegativeButton("ОК",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }


                }
        ).show();

    }

    public void appLogic(String button){

        String state = "U";

        if (button.equals("Yes") )  state="Y";
        if (currentMode.equals("First")){
            if (button.equals("No") || button.equals("Maybe")){

                setMode("Second");
                return;
            }
        } else{
            if (button.equals("Maybe")) state="T";
            if (button.equals("No")) state="U";
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String stringTime = df.format(new Date(System.currentTimeMillis()));
        wordCount += 1;
        String[] args = {state, stringTime, wordCount.toString(), wordId.toString()};

        SQLiteDatabase db = SQLiteDatabase.openDatabase(DB_PATH,null,SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = db.rawQuery("update words set state=?, lastdate=?, count=? where id= ?", args);
       // Log.d("u", state + "|"+stringTime  + "|"+ wordCount.toString() + "|"+wordId.toString());
        cursor.moveToFirst();
        cursor.close();
        db.close();
        setMode("First");
    }

    public void setMode(String mode){

        if (mode.equals( "First")){
            textView_rus.setVisibility(View.INVISIBLE);
            button_maybe.setVisibility(View.INVISIBLE);
            button_no.setText("Сомневаюсь");

            String searchState = "N";

            String[] args ={""};
            String sql;

            if ( wordType.equals("TOP_USE")) {
                args[0]="A";
                sql = "select id ,eng,rus,rating,count from words where state =? order by  count desc";
            }
            else{
                if ( wordType.equals("NEW")) {
                    searchState="N";
                }
                if ( wordType.equals("UNKNOWN")) {
                    searchState="U";
                }
                if ( wordType.equals("LEARN")) {
                    searchState="T";
                }
                args[0]=searchState;
                sql = "select id ,eng,rus,rating,count from words where state =? order by count, rating";
            }


            SQLiteDatabase db = SQLiteDatabase.openDatabase(DB_PATH,null,SQLiteDatabase.OPEN_READWRITE);
            Cursor cursor= db.rawQuery(sql,args);
            if (cursor.getCount()==0){
                Toast toast = Toast.makeText(getApplicationContext(),"Слова закончились", Toast.LENGTH_SHORT);
                toast.show();
                finish();
                return;
            }

            cursor.moveToFirst();
            wordId = cursor.getInt(0);
            textView_eng.setText(cursor.getString(1));
            textView_rus.setText(cursor.getString(2));
            textView_rating.setText(cursor.getString(3)); // ? string ?

            wordCount = cursor.getInt(4);
            textView_wordCount.setText(wordCount.toString()); // ? string ?
            cursor.close();
            db.close();

        }
        if (mode.equals("Second")){
            textView_rus.setVisibility(View.VISIBLE);
            button_maybe.setVisibility(View.VISIBLE);
            button_no.setText("Не знаю");
        }

        currentMode = mode;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_learn, menu);
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
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        db.close();
//    }
}
