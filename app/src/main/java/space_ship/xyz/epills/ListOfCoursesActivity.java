package space_ship.xyz.epills;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.sip.SipAudioCall;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.R.attr.id;

public class ListOfCoursesActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String LOG_TAG = "States";
    private DBHelper dbHelper;
    SQLiteDatabase database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_list);

        Log.d(LOG_TAG, "Main onCreate");

        //подключаемся к бд
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();


        TableLayout tableLayout = (TableLayout) findViewById(R.id.Table);

        //текуща дата строкой
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate=format1.format(new Date()).toString();
      // alert(currentDate); //25.02.2013

        //текущая дата в unix time
        Date date = null;
        try {
            date = format1.parse(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long dateNowUnix = (long)date.getTime()/1000;
      // alert(String.valueOf(unixTime));




        //Выбираем "Активные" курсы
        //date(substr(dateend,7,4) || '-' || substr(dateend,4,2) || '-' || substr(dateend,1,2))-сначала преобразует строку даты из бд в вид yyyy-mm-dd, а затем переводит в титп дата
        //strtime('%s', date(substr(dateend,7,4) || '-' || substr(dateend,4,2) || '-' || substr(dateend,1,2)))-возвращает юникс время. 86400 - однин день
        Cursor cursorActive = database.rawQuery("select _id, dateend,name,  date(substr(dateend,7,4) || '-' || substr(dateend,4,2) || '-' || substr(dateend,1,2)) from courses where " +
                "date(substr(dateend,7,4) || '-' || substr(dateend,4,2) || '-' || substr(dateend,1,2))  >= date('"+currentDate+"') AND" +
                " date(substr(datestart,7,4) || '-' || substr(datestart,4,2) || '-' || substr(datestart,1,2))  <= date('"+currentDate+"')", null);

        Cursor cursorFuture = database.rawQuery("select _id, name,  date(substr(datestart,7,4) || '-' || substr(datestart,4,2) || '-' || substr(datestart,1,2)) from courses where " +
                " date(substr(datestart,7,4) || '-' || substr(datestart,4,2) || '-' || substr(datestart,1,2))  > date('"+currentDate+"')", null);

        Cursor cursorPassive = database.rawQuery("select _id, dateend,name from courses where " +
                "date(substr(dateend,7,4) || '-' || substr(dateend,4,2) || '-' || substr(dateend,1,2))  < date('"+currentDate+"')", null);

        if(cursorActive.getCount()!=0 || cursorPassive.getCount()!=0 || cursorFuture.getCount()!=0) {
            //Если такие имеются выводим "Активные" и все курсы, входящие в эту группу
            if (cursorActive.moveToFirst()) {

                //СОздаем строчку АКТИВНЫЕ
                TableRow RowActive = new TableRow(this);
                RowActive.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
                TextView textActive = new TextView(this);
                textActive.setText("Активные");
                textActive.setTextSize(22);
                textActive.setPadding(35, 35, 0, 35);
                RowActive.addView(textActive);
                RowActive.setBackgroundColor(Color.LTGRAY);
                //поместим надпись в нашу таблицу
                tableLayout.addView(RowActive);

                //индексы в нашем запросы
                int dateEndIndex = cursorActive.getColumnIndex(DBHelper.KEY_COURSES_DATEEND);
                int nameInd = cursorActive.getColumnIndex(DBHelper.KEY_COURSES_NAME);
                int id = cursorActive.getColumnIndex(DBHelper.KEY_COURSES_ID);
                int dateStartInd = cursorActive.getColumnIndex("date(substr(dateend,7,4) || '-' || substr(dateend,4,2) || '-' || substr(dateend,1,2))"); //отвечает за возврат даты конца в формает yyyy-mm-dd

                String dateEndStr = "";
                Date dateEnd = null;
                long dateEndUnix, dateToEnd;

           /* String str="";
            for (String cn : cursor1.getColumnNames()) {
                str = str.concat(cn + " = "
                        + cursor1.getString(cursor1.getColumnIndex(cn)) + "; ");
            }
            alert(str);*/


                do {

                    dateEndStr = cursorActive.getString(dateStartInd); //строка из даты конца приема


                    try {
                        dateEnd = format1.parse(dateEndStr); //переводим строку в дату
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    dateEndUnix = (long) dateEnd.getTime() / 1000;  //переводим в юникс тайм

                    //считаем сколько осталось дней приема курса
                    dateToEnd = (dateEndUnix - dateNowUnix) / 86400;





                    //Создаем строку в таблице для курса
                   TableRow tableRow = new TableRow(this);
                    TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT);
                    tableRow.setLayoutParams(tableRowParams);
                    tableRow.setId(cursorActive.getInt(id)); //don't know if it is useful or not, but anyway
                    tableRow.setEnabled(true);
                    tableRow.setOnClickListener(this);  //установили слушителя
                    tableRow.setBackgroundColor(Color.GRAY);
                    tableRow.setPadding(0, 0, 0, 1); //для рамочки

                    //Создаем LinearLAyout для вставки в tablerow. В LinearLayout будет два текстовых поля с названием и с днями до конца приема
                    LinearLayout layout = new LinearLayout(this);
                    TableRow.LayoutParams llayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT, 1f);
                    layout.setLayoutParams(llayoutParams);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setBackgroundColor(Color.WHITE);

                    //Текст с названием лекарства
                    TextView textView1 = new TextView(this);
                    textView1.setTextSize(18);
                    textView1.setPadding(10, 10, 0, 0);
                    textView1.setText(cursorActive.getString(nameInd));


                    //Текст с кол-вом дней до конца приема
                    TextView textView2 = new TextView(this);
                    textView2.setTextSize(14);
                    textView2.setPadding(10, 10, 0/*1000*/, 10); //1000-отличный костыль на всякий случай

                    String day="";
                    if(dateToEnd%10==1)
                        day="день";
                    if(dateToEnd%10>=2 && dateToEnd%10<=4)
                        day="дня";
                    if(dateToEnd%10==0 || (dateToEnd%10>=5 && dateToEnd%10<=9) || (dateToEnd>=11 && dateToEnd<=14))
                        day="дней";
                    textView2.setText("До конца курса: " + dateToEnd+" "+day);

                    //добавляем наши тексты в лэйаут
                    layout.addView(textView1);
                    layout.addView(textView2);

                    //и запихиваем лэйаут в строчку
                    tableRow.addView(layout);

                    //А строчку в таблицу нашу перкрасную
                    tableLayout.addView(tableRow);

                    // alert(cursor1.getString(id)+""+"date: "+cursor1.getString(dateEndIndex));

                } while (cursorActive.moveToNext());


            }// pзакончили вывод активных





            if (cursorFuture.moveToFirst()) {

                //СОздаем строчку Запланированные
                TableRow RowFuture = new TableRow(this);
                RowFuture.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
                TextView textFuture = new TextView(this);
                textFuture.setText("Запланированные");
                textFuture.setTextSize(22);
                textFuture.setPadding(35, 35, 0, 35);
                RowFuture.addView(textFuture);
                RowFuture.setBackgroundColor(Color.LTGRAY);
                //поместим надпись в нашу таблицу
                tableLayout.addView(RowFuture);

                //индексы в нашем запросы
                int dateStartIndex = cursorFuture.getColumnIndex(DBHelper.KEY_COURSES_DATESTART);
                int nameInd = cursorFuture.getColumnIndex(DBHelper.KEY_COURSES_NAME);
                int id = cursorFuture.getColumnIndex(DBHelper.KEY_COURSES_ID);
                int dateStartInd = cursorFuture.getColumnIndex("date(substr(datestart,7,4) || '-' || substr(datestart,4,2) || '-' || substr(datestart,1,2))"); //отвечает за возврат даты конца в формает yyyy-mm-dd


                String dateStartStr = "";
                Date dateStart = null;
                long dateStartUnix, dateToEnd;

           /* String str="";
            for (String cn : cursor1.getColumnNames()) {
                str = str.concat(cn + " = "
                        + cursor1.getString(cursor1.getColumnIndex(cn)) + "; ");
            }
            alert(str);*/


                do {

                    dateStartStr = cursorFuture.getString(dateStartInd); //строка из даты конца приема


                    try {
                        dateStart = format1.parse(dateStartStr); //переводим строку в дату
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    dateStartUnix = (long) dateStart.getTime() / 1000;  //переводим в юникс тайм

                    //считаем сколько осталось дней приема курса
                    dateToEnd = (dateStartUnix - dateNowUnix) / 86400;





                    //Создаем строку в таблице для курса
                    TableRow tableRow = new TableRow(this);
                    TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT);
                    tableRow.setLayoutParams(tableRowParams);
                    tableRow.setId(cursorFuture.getInt(id)); //don't know if it is useful or not, but anyway
                    tableRow.setEnabled(true);
                    tableRow.setOnClickListener(this);  //установили слушителя
                    tableRow.setBackgroundColor(Color.GRAY);
                    tableRow.setPadding(0, 0, 0, 1); //для рамочки

                    //Создаем LinearLAyout для вставки в tablerow. В LinearLayout будет два текстовых поля с названием и с днями до конца приема
                    LinearLayout layout = new LinearLayout(this);
                    TableRow.LayoutParams llayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT, 1f);
                    layout.setLayoutParams(llayoutParams);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setBackgroundColor(Color.WHITE);

                    //Текст с названием лекарства
                    TextView textView1 = new TextView(this);
                    textView1.setTextSize(18);
                    textView1.setPadding(10, 10, 0, 0);
                    textView1.setText(cursorFuture.getString(nameInd));


                    //Текст с кол-вом дней до конца приема
                    TextView textView2 = new TextView(this);
                    textView2.setTextSize(14);
                    textView2.setPadding(10, 10, 0/*1000*/, 10); //1000-отличный костыль на всякий случай

                    String day="";
                    if(dateToEnd%10==1)
                        day="день";
                    if(dateToEnd%10>=2 && dateToEnd%10<=4)
                        day="дня";
                    if(dateToEnd%10==0 || (dateToEnd%10>=5 && dateToEnd%10<=9) || (dateToEnd>=11 && dateToEnd<=14))
                        day="дней";
                    textView2.setText("До начала курса: " + dateToEnd+" "+day);

                    //добавляем наши тексты в лэйаут
                    layout.addView(textView1);
                    layout.addView(textView2);

                    //и запихиваем лэйаут в строчку
                    tableRow.addView(layout);

                    //А строчку в таблицу нашу перкрасную
                    tableLayout.addView(tableRow);


                    // alert(cursor1.getString(id)+""+"date: "+cursor1.getString(dateEndIndex));

                } while (cursorFuture.moveToNext());


            }// pзакончили вывод pапланированных


            if (cursorPassive.moveToFirst()) {

                //СОздаем строчку ЗАВЕРШЕННЫЕ
               TableRow RowPassive = new TableRow(this);
               // TableRow RowPassive=(TableRow)getLayoutInflater().inflate(R.layout.layout_for_style, null,false);
                //alert(RowPassive.getClass().toString());

                //TableRow RowPassive = (TableRow) getLayoutInflater().inflate(R.layout.layout_for_style, null);

              RowPassive.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

               RowPassive.setBackgroundColor(getResources().getColor(R.color.ltgray));
                //TextView textPassive = new TextView(this);
                TextView textPassive = (TextView) getLayoutInflater().inflate(R.layout.test, null, false);
                textPassive.setText("Завершенные");
                textPassive.setTextSize(22);
                textPassive.setPadding(30, 30, 0, 30);
                RowPassive.addView(textPassive);
                //поместим надпись в нашу таблицу
                tableLayout.addView(RowPassive);

                //индексы в нашем запросы
                int dateEndIndex = cursorPassive.getColumnIndex(DBHelper.KEY_COURSES_DATEEND);
                int nameInd = cursorPassive.getColumnIndex(DBHelper.KEY_COURSES_NAME);
                int id = cursorPassive.getColumnIndex(DBHelper.KEY_COURSES_ID);



                do {

                    //Создаем строку в таблице для курса
                    //TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.layout_for_style, null);
                    TableRow tableRow = new TableRow(this);
                    TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT);
                    //tableRowParams.set
                    tableRow.setLayoutParams(tableRowParams);
                    tableRow.setId(cursorPassive.getInt(id)); //don't know if it is useful or not, but anyway
                    tableRow.setEnabled(true);
                    tableRow.setOnClickListener(this);  //установили слушителя
                    tableRow.setBackgroundColor(Color.GRAY);
                    tableRow.setPadding(0, 0, 0, 1); //для рамочки

                    //Создаем LinearLAyout для вставки в tablerow. В LinearLayout будет два текстовых поля с названием и с днями до конца приема
                    LinearLayout layout = new LinearLayout(this);
                    TableRow.LayoutParams llayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT, 1f);
                    layout.setLayoutParams(llayoutParams);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setBackgroundColor(Color.WHITE);

                    //Текст с названием лекарства
                    TextView textView1 = (TextView) getLayoutInflater().inflate(R.layout.test, null, false);
                   // TextView textView1 = new TextView(this);
                    textView1.setTextSize(18);
                    textView1.setPadding(10, 10, 0, 0);
                    textView1.setText(cursorPassive.getString(nameInd));

                    TextView textView2 = new TextView(this);
                    textView1.setTextSize(18);
                    textView1.setPadding(10, 10, 0, 0);


                    layout.addView(textView1);
                    layout.addView(textView2);
                    //и запихиваем лэйаут в строчку
                    tableRow.addView(layout);

                    //А строчку в таблицу нашу перкрасную
                    tableLayout.addView(tableRow);

                    // alert(cursor1.getString(id)+""+"date: "+cursor1.getString(dateEndIndex));

                } while (cursorPassive.moveToNext());


            }// pзакончили вывод законченных



        }
        else{
            TableRow RowNothing = new TableRow(this);

            RowNothing.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));


            TextView textNoth = (TextView) getLayoutInflater().inflate(R.layout.test, null, false);
            textNoth.setText("У Вас пока нет добавленных курсов");
            textNoth.setTextSize(20);
            textNoth.setPadding(20, 20, 0, 20);
            RowNothing.addView(textNoth);
            //поместим надпись в нашу таблицу
            tableLayout.addView(RowNothing);
            //если нет никакх лекарства
        }





//Просмотр всех записей
       Cursor cursorL = database.query(DBHelper.TABLE_COURSES, null, null, null, null, null, null);

        //cмотрим что в БД
       /* if(cursorL.moveToFirst())
        {
            int idIndex = cursorL.getColumnIndex(DBHelper.KEY_COURSES_ID);
            int nameIndex = cursorL.getColumnIndex(DBHelper.KEY_COURSES_NAME);
            int dateStartIndex = cursorL.getColumnIndex(DBHelper.KEY_COURSES_DATESTART);
            int dateEndIndex = cursorL.getColumnIndex(DBHelper.KEY_COURSES_DATEEND);
            int receptionsCountIndex = cursorL.getColumnIndex(DBHelper.KEY_COURSES_RECEPTIONSCOUNT);

            do {
                String tempDbCourseInfo =   "id: " + cursorL.getInt(idIndex) +
                        "\nname: " + cursorL.getString(nameIndex) +
                        "\ndateStart: " + cursorL.getString(dateStartIndex) +
                        "\ndateEnd: " + cursorL.getString(dateEndIndex) +
                        "\nreceptionsCount: " + cursorL.getInt(receptionsCountIndex) + "\n";



                alert(tempDbCourseInfo);

            } while(cursorL.moveToNext());
        }
        else
        {
            alert("nothing in DB");
        }
        cursorL.close();*/
        cursorActive.close();
        cursorPassive.close();

    }


    public void alert(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Важное сообщение!")
                .setMessage(text)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_course_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //обработка нажатия кнопки меню
    public boolean onOptionsItemSelected(MenuItem item) {

    // получим идентификатор выбранного пункта меню
    int id = item.getItemId();

    // Операции для выбранного пункта меню
    switch (id) {
        case R.id.add_new: //переходим на активность добавления лекарства
            //Intent intent = new Intent(ListOfCoursesActivity.this, CourseEditActivity.class);
            //startActivity(intent);
            Intent intent = new Intent(this, CourseEditActivity.class);
            startActivityForResult(intent, 1);
            return true;
        default:
            return super.onOptionsItemSelected(item);

    }
    }

    //для обработки нажатия на лекарства
    @Override
    public void onClick(View v) {
       //Something like this but with other Activity
       /*  Intent intent = new Intent(ListOfCoursesActivity.this, CourseEditActivity.class);
        startActivity(intent);*/
      //  String t=tableRow.;
        Intent intent = new Intent(ListOfCoursesActivity.this,InfoCourseActivity.class);
               int id=v.getId();
               intent.putExtra("course_id",id);
               startActivity(intent);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent refresh = new Intent(this, ListOfCoursesActivity.class);
        startActivity(refresh);
        this.finish();
    }


}
