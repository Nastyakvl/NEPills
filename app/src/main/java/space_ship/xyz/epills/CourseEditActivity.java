package space_ship.xyz.epills;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.sip.SipAudioCall;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.text.format.DateUtils;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TableRow;

import android.app.AlertDialog;
import android.content.DialogInterface;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;


public class CourseEditActivity extends AppCompatActivity {
    private static final String LOG_TAG = "States";

    private Toolbar toolbar;

    private DBHelper dbHelper;
    private SQLiteDatabase database;

    TextView currentDateTime;
    Calendar dateAndTime = Calendar.getInstance();

    //поля курса
    EditText course_edit_name;
    EditText course_edit_date_start;
    EditText course_edit_date_end;
    TextView course_edit_count;

    //указатели на объекты приёма
    TableRow receptions[] = new TableRow[10];
    EditText receptionsTime[] = new EditText[10];
    EditText receptionsCount[] = new EditText[10];
    int editingReceptionTimeI = 0;

    //дефолтные время начала и конца приёмов
    int receptionsTimeStart = 8;
    int receptionsTimeEnd = 21;

    String calID="";

    int editingCourseID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Получаем _id лекарства
        Intent intent = getIntent();
        final String course_id = String.valueOf(intent.getIntExtra("course_id", 0));
        editingCourseID = Integer.parseInt(course_id);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle( ( editingCourseID == 0  ? "Добавление" : "Редактирование" ) + " курса");
        setSupportActionBar(toolbar);

        dbHelper = new DBHelper(this);

        // —---------------------Находим календарь--------------------—
        Uri calendars = Uri.parse("content://com.android.calendar/calendars");
        String[] projection = new String[]{"_id", "name"};
        String calName;
//String calID="";
        Cursor managedCursor = this.managedQuery(calendars, projection, null, null, null);
        if (managedCursor != null && managedCursor.moveToFirst()) {
            int nameColumn = managedCursor.getColumnIndex("name");
            int idColumn = managedCursor.getColumnIndex("_id");

            calName = managedCursor.getString(nameColumn);
            calID = managedCursor.getString(idColumn);
// alert(calName+""+calID);
// if (calName != null) // … UI

        } else alert("Err");
        managedCursor.close();

//-----------------------------------------------


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_course_edit_button2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //событие добавления/редактирования

                //заполнение данных
                String name                 = course_edit_name.getText().toString();
                String date_start           = course_edit_date_start.getText().toString();
                String date_end             = course_edit_date_end.getText().toString();
                int receptions_count        = Integer.parseInt(course_edit_count.getText().toString());
                String receptions_info[][]  = new String[receptions_count][2];
                for (int i = 0; i < receptions_count; i++) {
                    receptions_info[i][0] = receptionsTime[i].getText().toString();
                    receptions_info[i][1] = receptionsCount[i].getText().toString();
                }

                //промежуточное выведение данных(для отладки)
                /*String tempCourseInfo = "name: " + name +
                                        "\ndateStart: " + date_start +
                                        "\ndateEnd: " + date_end +
                                        "\nreceptionsCount: " + receptions_count + "\n";
                for (int i = 0; i < receptions_count; i++)
                {
                    tempCourseInfo += "\n" + (i+1) + ": " + receptions_info[i][1] + " at " + receptions_info[i][0];
                }
                alert(tempCourseInfo);*/

                //проверка на корректность заполнения
                if(name.length() == 0 || date_start.length() == 0 || date_end.length() == 0)
                {
                    Snackbar.make(view, "Проверьте корректность заполнения полей!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                //если данные заполнены корректно - добавляем в БД
                else
                {
                    boolean status = true;

                    SQLiteDatabase database = dbHelper.getWritableDatabase();

                    //очистка БД
                   //database.delete(DBHelper.TABLE_COURSES, null, null);
                   //database.delete(DBHelper.TABLE_RECEPTIONS, null, null);

                    if(editingCourseID == 0) {

                        ContentValues data = new ContentValues();
                        data.put(DBHelper.KEY_COURSES_NAME, name);
                        data.put(DBHelper.KEY_COURSES_DATESTART, date_start);


                        data.put(DBHelper.KEY_COURSES_DATEEND, date_end);
                        data.put(DBHelper.KEY_COURSES_RECEPTIONSCOUNT, receptions_count);


                        int courseId = Integer.parseInt(new String("" + database.insert(dbHelper.TABLE_COURSES, null, data)));

                        //если успешно - добавляем приёмы
                        if (courseId > 0) {

                            for (int i = 0; i < receptions_count; i++) {
                                data.clear();
                                data.put(DBHelper.KEY_RECEPTIONS_COURSEID, courseId);
                                data.put(DBHelper.KEY_RECEPTIONS_TIME, receptions_info[i][0]);
                                data.put(DBHelper.KEY_RECEPTIONS_COUNT, receptions_info[i][1]);


                               //-----------------Для напоминания---------------
                                Date dateNotif=null;
                                SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy hh:mm");
                                long startMillis = 0;
                                long endMillis = 0;
                                try {
                                    dateNotif = format1.parse(date_start+" "+receptions_info[i][0]); //переводим строку в дату
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                //startMillis = (long) dateNotif.getTime() / 1000;

                                Calendar beginTime = Calendar.getInstance();
                                beginTime.setTime(dateNotif);

                                startMillis = beginTime.getTimeInMillis();

                                endMillis = startMillis +900000;

                                long end;
                                try {
                                    dateNotif = format1.parse(date_end+" "+receptions_info[i][0]); //переводим строку в дату
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                //startMillis = (long) dateNotif.getTime() / 1000;

                                Calendar endTime = Calendar.getInstance();
                                endTime.setTime(dateNotif);

                                end = endTime.getTimeInMillis();

                                int count=(int) ( (end-startMillis)/(24 * 60 * 60 * 1000))+1;
                               // alert(String.valueOf(count));

                                //alert("count "+String.valueOf(count));
                               // alert("minus "+String.valueOf(end-startMillis));



                                /* try {
                                    dateNotif = format1.parse(date_end+" "+receptions_info[i][0]); //переводим строку в дату
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                beginTime.setTime(dateNotif);
                                String until=String.valueOf(beginTime.getTimeInMillis());*/


                                ContentResolver cr = getContentResolver();
                                ContentValues values = new ContentValues();
                                values.put(CalendarContract.Events.DTSTART, startMillis);
                                values.put(CalendarContract.Events.DTEND, endMillis);
                                values.put(CalendarContract.Events.TITLE, "Время пить лекарство: "+name);
                                values.put(CalendarContract.Events.DESCRIPTION, receptions_info[i][0]);
                                values.put(CalendarContract.Events.CALENDAR_ID, calID);
                                values.put(CalendarContract.Events.EVENT_TIMEZONE, "Russia/Moscow");
                                values.put(CalendarContract.Events.RRULE, "FREQ=DAILY;COUNT="+String.valueOf(count)+";");

                                Uri uri=null;
                                try {
                                    uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                                }
                                catch(SecurityException e){alert(e.toString());}

                               long eventID =Long.parseLong(uri.getLastPathSegment());


                                ContentResolver cr2 = getContentResolver();
                                ContentValues values2 = new ContentValues();
                                values2.put(CalendarContract.Reminders.MINUTES, 0);
                                values2.put(CalendarContract.Reminders.EVENT_ID, eventID);
                                values2.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                                try {
                                    Uri uruu = cr2.insert(CalendarContract.Reminders.CONTENT_URI, values2);
                                }
                                catch(SecurityException e){}

                                //-------------------------------------------------------------------



                                //Добавляем данные в таблицу course_event
                                ContentValues data2 = new ContentValues();

                                data2.put(DBHelper.KEY_COURSE_EVENT_COURSEID,courseId);
                                data2.put(DBHelper.KEY_COURSE_EVENT_EVENTID,eventID);
                                database.insert(dbHelper.TABLE_COURSE_EVENT, null, data2);

                            /*   Cursor eventInfo = database.query(DBHelper.TABLE_COURSE_EVENT, null, null, null, null, null, null);
                                if(eventInfo.moveToFirst()) {

                                    int idIndex = eventInfo.getColumnIndex(DBHelper.KEY_COURSE_EVENT_COURSEID);
                                    int nameIndex = eventInfo.getColumnIndex(DBHelper.KEY_COURSE_EVENT_EVENTID);
                                    do {
                                        String tempDbCourseInfo =   "idCourse: " +eventInfo.getInt(idIndex) +
                                                "\nidEvent: " + eventInfo.getString(nameIndex);



                                        alert(tempDbCourseInfo);

                                    } while(eventInfo.moveToNext());
                                }
                                else {alert("NOTHING");}*/


                                if (database.insert(dbHelper.TABLE_RECEPTIONS, null, data) == -1) {
                                    status = false;
                                }
                            }

                        } else {
                            status = false;
                        }
                    }
                    //редактирование
                    else
                    {
                        //Находим все eventID
                        String table = "course_event";
                        String[] columns = new String[]{"event_id"};
                        String selection = "course_id = ?";
                        String[] selectionArgs = new String[]{course_id};
                        Cursor eventInfo = database.query(table, columns, selection, selectionArgs, null, null, null);
                        //Удаляем события
                        if (eventInfo.moveToFirst())
                        {
                            do
                            {
                                long eventID = eventInfo.getLong(eventInfo.getColumnIndex(DBHelper.KEY_COURSE_EVENT_EVENTID));
                                ContentResolver cr = getContentResolver();
                                ContentValues values = new ContentValues();
                                //Uri deleteUri = null;
                                Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
                                int rows = getContentResolver().delete(deleteUri, null, null);

                            }while (eventInfo.moveToNext());

                        }

                        //Удаляем все eventId по courseId
                        database.delete(DBHelper.TABLE_COURSE_EVENT, "course_id = ?",
                                new String[] {course_id});




                        ContentValues data = new ContentValues();
                        data.put(DBHelper.KEY_COURSES_NAME, name);
                        data.put(DBHelper.KEY_COURSES_DATESTART, date_start);


                        data.put(DBHelper.KEY_COURSES_DATEEND, date_end);
                        data.put(DBHelper.KEY_COURSES_RECEPTIONSCOUNT, receptions_count);

                        int result = database.update(dbHelper.TABLE_COURSES, data, dbHelper.KEY_COURSES_ID + "= ?",
                                new String[] { String.valueOf(editingCourseID) });

                        //удаление напоминаний
                        database.delete(dbHelper.TABLE_RECEPTIONS, dbHelper.KEY_RECEPTIONS_COURSEID + "= ?",
                                new String[] { String.valueOf(editingCourseID) });

                        for (int i = 0; i < receptions_count; i++) {
                            data.clear();
                            data.put(DBHelper.KEY_RECEPTIONS_COURSEID, editingCourseID);
                            data.put(DBHelper.KEY_RECEPTIONS_TIME, receptions_info[i][0]);
                            data.put(DBHelper.KEY_RECEPTIONS_COUNT, receptions_info[i][1]);

                            database.insert(dbHelper.TABLE_RECEPTIONS, null, data);

                            //-----------------Для напоминания---------------
                            Date dateNotif=null;
                            SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy hh:mm");
                            long startMillis = 0;
                            long endMillis = 0;
                            try {
                                dateNotif = format1.parse(date_start+" "+receptions_info[i][0]); //переводим строку в дату
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            //startMillis = (long) dateNotif.getTime() / 1000;

                            Calendar beginTime = Calendar.getInstance();
                            beginTime.setTime(dateNotif);

                            startMillis = beginTime.getTimeInMillis();

                            endMillis = startMillis +900000;

                            long end;
                            try {
                                dateNotif = format1.parse(date_end+" "+receptions_info[i][0]); //переводим строку в дату
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            //startMillis = (long) dateNotif.getTime() / 1000;

                            Calendar endTime = Calendar.getInstance();
                            endTime.setTime(dateNotif);

                            end = endTime.getTimeInMillis();

                            int count=(int) ( (end-startMillis)/(24 * 60 * 60 * 1000))+1;

                            ContentResolver cr = getContentResolver();
                            ContentValues values = new ContentValues();
                            values.put(CalendarContract.Events.DTSTART, startMillis);
                            values.put(CalendarContract.Events.DTEND, endMillis);
                            values.put(CalendarContract.Events.TITLE, "Время пить лекарство: "+name);
                            values.put(CalendarContract.Events.DESCRIPTION, receptions_info[i][0]);
                            values.put(CalendarContract.Events.CALENDAR_ID, calID);
                            values.put(CalendarContract.Events.EVENT_TIMEZONE, "Russia/Moscow");
                            values.put(CalendarContract.Events.RRULE, "FREQ=DAILY;COUNT="+String.valueOf(count)+";");

                            Uri uri=null;
                            try {
                                uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                            }
                            catch(SecurityException e){alert(e.toString());}

                            long eventID =Long.parseLong(uri.getLastPathSegment());


                            ContentResolver cr2 = getContentResolver();
                            ContentValues values2 = new ContentValues();
                            values2.put(CalendarContract.Reminders.MINUTES, 0);
                            values2.put(CalendarContract.Reminders.EVENT_ID, eventID);
                            values2.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                            try {
                                Uri uruu = cr2.insert(CalendarContract.Reminders.CONTENT_URI, values2);
                            }
                            catch(SecurityException e){}

                            //-------------------------------------------------------------------



                            //Добавляем данные в таблицу course_event
                            ContentValues data2 = new ContentValues();

                            data2.put(DBHelper.KEY_COURSE_EVENT_COURSEID,course_id);
                            data2.put(DBHelper.KEY_COURSE_EVENT_EVENTID,eventID);
                            database.insert(dbHelper.TABLE_COURSE_EVENT, null, data2);

                        }






                    }

                    if(status)
                    {
                       // Snackbar.make(view, "Курс успешно добавлен!", Snackbar.LENGTH_LONG)
                             //   .setAction("Action", null).show();

                        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        // всё, данные в базе. Теперь можно возвращаться на
                        //  предидущую страницу и обновлять список курсов
                        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        //Переходим на новую Активити со всеми курсами

                       // Intent intent = new Intent(CourseEditActivity.this, ListOfCoursesActivity.class);
                        //startActivity(intent);
                       // finish();
                        if(editingCourseID==0){ Intent intent = new Intent();
                            intent.putExtra("name", name);
                            // setResult(RESULT_OK, intent);
                            finish();
                        }
                        else {
                            Intent intent = new Intent();
                            intent.putExtra("name", name);
                            // setResult(RESULT_OK, intent);
                            //getParent().getParent().finish();
                            //getParent().finish();
                           // getParent().getParent().getParent().finish();
                            finish();

                        }

                    }
                    else
                    {
                        Snackbar.make(view, "Ошибка при добавлении/редактировании курса!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }


                    //========= выведение данных =========
                   /* Cursor cursor = database.query(DBHelper.TABLE_COURSES, null, null, null, null, null, null);

                    if(cursor.moveToFirst())
                    {
                        int idIndex = cursor.getColumnIndex(DBHelper.KEY_COURSES_ID);
                        int nameIndex = cursor.getColumnIndex(DBHelper.KEY_COURSES_NAME);
                        int dateStartIndex = cursor.getColumnIndex(DBHelper.KEY_COURSES_DATESTART);
                        int dateEndIndex = cursor.getColumnIndex(DBHelper.KEY_COURSES_DATEEND);
                        int receptionsCountIndex = cursor.getColumnIndex(DBHelper.KEY_COURSES_RECEPTIONSCOUNT);

                        do {
                            String tempDbCourseInfo =   "id: " + cursor.getInt(idIndex) +
                                                        "\nname: " + cursor.getString(nameIndex) +
                                                        "\ndateStart: " + cursor.getString(dateStartIndex) +
                                                        "\ndateEnd: " + cursor.getString(dateEndIndex) +
                                                        "\nreceptionsCount: " + cursor.getInt(receptionsCountIndex) + "\n";

                            Cursor cursor2 = database.query(    DBHelper.TABLE_RECEPTIONS, null,
                                                                DBHelper.KEY_RECEPTIONS_COURSEID + " = " + cursor.getInt(idIndex), null, null, null, null);

                            if(cursor2.moveToFirst())
                            {
                                int timeIndex = cursor2.getColumnIndex(DBHelper.KEY_RECEPTIONS_TIME);
                                int countIndex = cursor2.getColumnIndex(DBHelper.KEY_RECEPTIONS_COUNT);

                                int i = 1;
                                do {
                                    tempDbCourseInfo += "\n" + i + ": " + cursor2.getString(countIndex) + " at " + cursor2.getString(timeIndex);
                                    i++;
                                } while(cursor2.moveToNext());
                            }
                            cursor2.close();

                            alert(tempDbCourseInfo);

                        } while(cursor.moveToNext());
                    }
                    else
                    {
                        alert("nothing in DB");
                    }
                    cursor.close();*/
                    //========= КОНЕЦ выведение данных КОНЕЦ =========




                }
            }
        });

        //заполнение указателей на объекты
        course_edit_name = (EditText) findViewById(R.id.course_edit_name);
        course_edit_date_start = (EditText) findViewById(R.id.course_edit_date_start);
        course_edit_date_end = (EditText) findViewById(R.id.course_edit_date_end);
        course_edit_count = (TextView) findViewById(R.id.course_edit_count);

        receptions[0]       = (TableRow) findViewById(R.id.reception_1);
        receptions[1]       = (TableRow) findViewById(R.id.reception_2);
        receptions[2]       = (TableRow) findViewById(R.id.reception_3);
        receptions[3]       = (TableRow) findViewById(R.id.reception_4);
        receptions[4]       = (TableRow) findViewById(R.id.reception_5);
        receptions[5]       = (TableRow) findViewById(R.id.reception_6);
        receptions[6]       = (TableRow) findViewById(R.id.reception_7);
        receptions[7]       = (TableRow) findViewById(R.id.reception_8);
        receptions[8]       = (TableRow) findViewById(R.id.reception_9);
        receptions[9]       = (TableRow) findViewById(R.id.reception_10);
        receptionsTime[0]   = (EditText) findViewById(R.id.reception_1_time);
        receptionsTime[1]   = (EditText) findViewById(R.id.reception_2_time);
        receptionsTime[2]   = (EditText) findViewById(R.id.reception_3_time);
        receptionsTime[3]   = (EditText) findViewById(R.id.reception_4_time);
        receptionsTime[4]   = (EditText) findViewById(R.id.reception_5_time);
        receptionsTime[5]   = (EditText) findViewById(R.id.reception_6_time);
        receptionsTime[6]   = (EditText) findViewById(R.id.reception_7_time);
        receptionsTime[7]   = (EditText) findViewById(R.id.reception_8_time);
        receptionsTime[8]   = (EditText) findViewById(R.id.reception_9_time);
        receptionsTime[9]   = (EditText) findViewById(R.id.reception_10_time);
        receptionsCount[0]  = (EditText) findViewById(R.id.reception_1_count);
        receptionsCount[1]  = (EditText) findViewById(R.id.reception_2_count);
        receptionsCount[2]  = (EditText) findViewById(R.id.reception_3_count);
        receptionsCount[3]  = (EditText) findViewById(R.id.reception_4_count);
        receptionsCount[4]  = (EditText) findViewById(R.id.reception_5_count);
        receptionsCount[5]  = (EditText) findViewById(R.id.reception_6_count);
        receptionsCount[6]  = (EditText) findViewById(R.id.reception_7_count);
        receptionsCount[7]  = (EditText) findViewById(R.id.reception_8_count);
        receptionsCount[8]  = (EditText) findViewById(R.id.reception_9_count);
        receptionsCount[9]  = (EditText) findViewById(R.id.reception_10_count);

        //установка Listenerov на события клика
        course_edit_date_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(ds);



            }

        });
        course_edit_date_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(de);
            }

        });
        for (int i = 0; i < 10; i++) {
            receptionsTime[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setTime(v.getId());
                }
            });
        }

        if(editingCourseID > 0)
        {
            //подключаемся к бд
            dbHelper = new DBHelper(this);
            database = dbHelper.getWritableDatabase();

            //Получаем название, начало и конец приема лекарства из БД
            String table_info = "courses";
            String[] columns_info = new String[]{"name", "datestart", "dateend"};
            String selection_info = "_id = ?";
            String[] selectionArgs_info = new String[]{course_id};
            Cursor info = database.query(table_info, columns_info, selection_info, selectionArgs_info, null, null, null);

            //Выводим данные
            if (info.getColumnCount() != 0) {
                if (info.moveToFirst()) {
                    TextView course_name = (TextView) findViewById(R.id.course_name);
                    course_edit_name.setText(info.getString(info.getColumnIndex(DBHelper.KEY_COURSES_NAME)));

                    TextView course_dateStart = (TextView) findViewById(R.id.course_start);
                    course_edit_date_start.setText(info.getString(info.getColumnIndex(DBHelper.KEY_COURSES_DATESTART)));

                    TextView course_dateEnd = (TextView) findViewById(R.id.course_end);
                    course_edit_date_end.setText(info.getString(info.getColumnIndex(DBHelper.KEY_COURSES_DATEEND)));

                    //Получаем кол-во приемов, время
                    String table_time = "receptions";
                    String[] columns_time = new String[]{"time", "count"};
                    String selection_time = DBHelper.KEY_RECEPTIONS_COURSEID + " = ?";
                    String[] selectionArgs_time = new String[]{course_id};
                    Cursor time = database.query(table_time, columns_time, selection_time, selectionArgs_time, null, null, null);

                    //Выводим данные
                    int receptions_count = time.getCount();//кол-во приемов
                    counterUpdate(receptions_count);
                    int i = 0;
                    if (receptions_count != 0) {
                        if (time.moveToFirst()) {
                            do {
                                try {

                                    receptionsCount[i].setText(time.getString(time.getColumnIndex(DBHelper.KEY_RECEPTIONS_COUNT)));
                                    receptionsTime[i].setText(time.getString(time.getColumnIndex(DBHelper.KEY_RECEPTIONS_TIME)));

                                } catch (Exception e) {
                                    alert(e.getMessage());
                                }
                                i++;
                            } while (time.moveToNext());
                            time.close();
                        }
                    }

                } else {
                    alert("NOTHING in DB");
                }
            } else {
                alert("nothing in DB");
            }
            info.close();
        }
        else
        {
            //установка дефолтного количества приёмов
            counterUpdate(2);
        }

        //установка дефолтных дат
        //course_edit_date_start.setText("" + dateAndTime.get(Calendar.DAY_OF_MONTH) + "." + dateAndTime.get(Calendar.MONTH) + "." + dateAndTime.get(Calendar.YEAR));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_course_edit, menu);
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

    //управление счётчиком приёмов
    public void countPlus(View view)
    {
        int counter = Integer.parseInt(course_edit_count.getText().toString()) + 1;

        if(counter > 10)
        {
            counter = 10;
        }

        counterUpdate(counter);
    }
    public void countMinus(View view)
    {
        int counter = Integer.parseInt(course_edit_count.getText().toString()) - 1;

        if (counter < 1)
        {
            counter = 1;
        }

        counterUpdate(counter);
    }

    //обновление количества приёмов
    private void counterUpdate( int receptionsCount )
    {
        //лбновление счётчика
        String temp = "" + receptionsCount;
        course_edit_count.setText(temp);

        //отображение нужного количества приёмов
        for(int i=0; i < 10; i++)
        {
            receptions[i].setVisibility( (i < receptionsCount ? View.VISIBLE : View.GONE) );
        }

        //распределение времени
        int timeHour[]      = new int[receptionsCount];
        int timeMinute[]    = new int[receptionsCount];
        timeHour[0]     = receptionsTimeStart;
        timeMinute[0]   = 0;
        if( receptionsCount > 1 )
        {
            timeHour[receptionsCount-1]     = receptionsTimeEnd;
            timeMinute[receptionsCount-1]   = 0;
        }
        if( receptionsCount > 2 ) {
            double receptionsInterval  = receptionsTimeEnd - receptionsTimeStart;
            double receptionsStep    = receptionsInterval / (((double) receptionsCount) - ((double) 1));

            double  tempCounter = 0;
            double  temp1       = 0;
            double  temp2       = 0;
            int     temp3       = 0;
            for (int i = 1; i < receptionsCount-1; i++) {
                tempCounter += receptionsStep;

                temp1 = new BigDecimal(tempCounter).setScale(2, RoundingMode.UP).doubleValue();

                //установка часа приёма
                temp3 = (int) temp1;
                timeHour[i] = temp3 + receptionsTimeStart;
                //установка миут приёма
                temp2 = temp1 - temp3; //дробная часть
                timeMinute[i] = (temp2 < 0.5 ? 0 : 30);
                //alert("" + receptionsInterval + "/" + receptionsStep + "/" + temp1 + "/" + temp2);
            }
        }

        //установка времени
        for (int i = 0; i < receptionsCount; i++)
        {
            temp = "" + (timeHour[i] < 10 ? "0" : "") + timeHour[i] + ":" + (timeMinute[i] < 10 ? "0" : "") + timeMinute[i];
            receptionsTime[i].setText(temp);
        }
    }

    // отображаем диалоговое окно для выбора начальной даты
    public void setDate(DatePickerDialog.OnDateSetListener listener) {
        new DatePickerDialog(CourseEditActivity.this, listener,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // отображаем диалоговое окно для выбора времени
    public void setTime(int editingID) {
        //alert(""+editingID + "/" + R.id.reception_1_time);
        if(editingID == R.id.reception_1_time)
            editingReceptionTimeI = 0;
        else if(editingID == R.id.reception_2_time)
            editingReceptionTimeI = 1;
        else if(editingID == R.id.reception_3_time)
            editingReceptionTimeI = 2;
        else if(editingID == R.id.reception_4_time)
            editingReceptionTimeI = 3;
        else if(editingID == R.id.reception_5_time)
            editingReceptionTimeI = 4;
        else if(editingID == R.id.reception_6_time)
            editingReceptionTimeI = 5;
        else if(editingID == R.id.reception_7_time)
            editingReceptionTimeI = 6;
        else if(editingID == R.id.reception_8_time)
            editingReceptionTimeI = 7;
        else if(editingID == R.id.reception_9_time)
            editingReceptionTimeI = 8;
        else if(editingID == R.id.reception_10_time)
            editingReceptionTimeI = 9;

        String temp = receptionsTime[editingReceptionTimeI].getText().toString();
        String[] parts = temp.split(":");

        new TimePickerDialog(CourseEditActivity.this, t,
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]), true)
                .show();
    }



    // установка обработчиков выбора даты
    DatePickerDialog.OnDateSetListener ds = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if ((dayOfMonth>=1 && dayOfMonth<=9) && ((monthOfYear+1)>=1 && (monthOfYear+1)<=9))
                course_edit_date_start.setText("0"+dayOfMonth+ "." + "0"+(monthOfYear + 1) + "." + year);
            else
                if (dayOfMonth >= 1 && dayOfMonth <= 9)
                    course_edit_date_start.setText("0" + dayOfMonth + "." + (monthOfYear + 1) + "." + year);

                else
                    if ((monthOfYear+1)>=1 && (monthOfYear+1)<=9)
                    course_edit_date_start.setText(dayOfMonth+ "." + "0"+(monthOfYear + 1) + "." + year);

                     else course_edit_date_start.setText(dayOfMonth + "." + (monthOfYear + 1) + "." + year);

           /* if (dayOfMonth>=1 && dayOfMonth<=9)
                course_edit_date_start.setText("0"+dayOfMonth+ "." + (monthOfYear + 1) + "." + year);
            else   course_edit_date_start.setText(dayOfMonth+ "." + (monthOfYear + 1) + "." + year);*/
            //course_edit_date_start.setText(dayOfMonth + "." + (monthOfYear + 1) + "." + year);
        }
    };
    DatePickerDialog.OnDateSetListener de = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            if ((dayOfMonth>=1 && dayOfMonth<=9) && ((monthOfYear+1)>=1 && (monthOfYear+1)<=9))
                course_edit_date_end.setText("0"+dayOfMonth+ "." + "0"+(monthOfYear + 1) + "." + year);
            else
            if (dayOfMonth >= 1 && dayOfMonth <= 9)
                course_edit_date_end.setText("0" + dayOfMonth + "." + (monthOfYear + 1) + "." + year);

            else
            if ((monthOfYear+1)>=1 && (monthOfYear+1)<=9)
                course_edit_date_end.setText(dayOfMonth+ "." + "0"+(monthOfYear + 1) + "." + year);

            else course_edit_date_end.setText(dayOfMonth + "." + (monthOfYear + 1) + "." + year);

           // course_edit_date_end.setText(dayOfMonth + "." + (monthOfYear + 1) + "." + year);
            /*if (dayOfMonth>=1 && dayOfMonth<=9)
            course_edit_date_end.setText("0"+dayOfMonth+ "." + (monthOfYear + 1) + "." + year);
            else   course_edit_date_end.setText(dayOfMonth+ "." + (monthOfYear + 1) + "." + year);*/
           // course_edit_date_end.setText(dayOfMonth+ "." + (monthOfYear + 1) + "." + year);
        }
    };

    // установка обработчика выбора времени
    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            receptionsTime[editingReceptionTimeI].setText("" + (hourOfDay < 10 ? "0" : "") + hourOfDay + ":" + (minute < 10 ? "0" : "") + minute);
        }
    };

    //вывод диалогового окна
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



}
