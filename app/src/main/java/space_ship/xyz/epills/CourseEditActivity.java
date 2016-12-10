package space_ship.xyz.epills;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.sip.SipAudioCall;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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



public class CourseEditActivity extends AppCompatActivity {
    private static final String LOG_TAG = "States";

    private Toolbar toolbar;

    private DBHelper dbHelper;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_edit);

        Log.d(LOG_TAG, "ADD onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Добавление курса");
        setSupportActionBar(toolbar);

        dbHelper = new DBHelper(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_course_edit_button2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //событие добавления

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
                    Snackbar.make(view, "Проверьте корректность заполнения поленй!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                //если данные заполнены корректно - добавляем в БД
                else
                {
                    boolean status = true;

                    SQLiteDatabase database = dbHelper.getWritableDatabase();

                    //очистка БД
                  // database.delete(DBHelper.TABLE_COURSES, null, null);
                   //database.delete(DBHelper.TABLE_RECEPTIONS, null, null);

                    ContentValues data = new ContentValues();
                    data.put(DBHelper.KEY_COURSES_NAME, name);
                    data.put(DBHelper.KEY_COURSES_DATESTART, date_start);


                    data.put(DBHelper.KEY_COURSES_DATEEND, date_end);
                    data.put(DBHelper.KEY_COURSES_RECEPTIONSCOUNT, receptions_count);

                    int courseId = Integer.parseInt( new String( "" + database.insert(dbHelper.TABLE_COURSES, null, data) ));

                    //если успешно - добавляем приёмы
                    if( courseId > 0 ) {

                        for (int i = 0; i < receptions_count; i++)
                        {
                            data.clear();
                            data.put(DBHelper.KEY_RECEPTIONS_COURSEID, courseId);
                            data.put(DBHelper.KEY_RECEPTIONS_TIME, receptions_info[i][0]);
                            data.put(DBHelper.KEY_RECEPTIONS_COUNT, receptions_info[i][1]);

                            if(database.insert(dbHelper.TABLE_RECEPTIONS, null, data) == -1)
                            {
                                status = false;
                            }
                        }

                    }
                    else
                    {
                        status = false;
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

                        Intent intent = new Intent();
                        intent.putExtra("name", name);
                       // setResult(RESULT_OK, intent);
                        finish();

                    }
                    else
                    {
                        Snackbar.make(view, "Ошибка при добавлении курса!", Snackbar.LENGTH_LONG)
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

        //установка дефолтного количества приёмов
        counterUpdate(2);

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
