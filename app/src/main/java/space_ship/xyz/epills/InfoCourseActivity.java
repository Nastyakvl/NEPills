package space_ship.xyz.epills;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.sip.SipAudioCall;
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
import android.widget.Button;
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

public class InfoCourseActivity extends AppCompatActivity {

    private static final String LOG_TAG = "States";
    private static final String DEBUG_TAG = "MyActivity";
    private DBHelper dbHelper;
    private SQLiteDatabase database;

    int course_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_course);

        //подключаемся к бд
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        final AlertDialog.Builder delete = new AlertDialog.Builder(this);

        //Получаем _id лекарства
        Intent intent = getIntent();
        final String course_id = String.valueOf(intent.getIntExtra("course_id", 0));
        final int courseID = Integer.parseInt(course_id);
        this.course_ID = courseID;

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
                course_name.setText(info.getString(info.getColumnIndex(DBHelper.KEY_COURSES_NAME)));

                TextView course_dateStart = (TextView) findViewById(R.id.course_start);
                course_dateStart.setText(info.getString(info.getColumnIndex(DBHelper.KEY_COURSES_DATESTART)));

                TextView course_dateEnd = (TextView) findViewById(R.id.course_end);
                course_dateEnd.setText(info.getString(info.getColumnIndex(DBHelper.KEY_COURSES_DATEEND)));
            } else {
                alert("NOTHING in DB");
            }
        } else {
            alert("nothing in DB");
        }
        info.close();

        //Получаем кол-во приемов, время
        String table_time = "receptions";
        String[] columns_time = new String[]{"time", "count"};
        String selection_time = DBHelper.KEY_RECEPTIONS_COURSEID + " = ?";
        String[] selectionArgs_time = new String[]{course_id};
        Cursor time = database.query(table_time, columns_time, selection_time, selectionArgs_time, null, null, null);

        //Выводим данные
        TableLayout tableLayout = (TableLayout) findViewById(R.id.table);
        int receptions_count = time.getCount();//кол-во приемов
        int i = 1;
        if (receptions_count != 0) {
            if (time.moveToFirst()) {
                do {
                    try {

                        tableLayout.setStretchAllColumns(true);

                        TextView textView = new TextView(this);
                        textView.setTextSize(17);
                        textView.setTextColor(Color.BLACK);
                        textView.setText("                     ");

                        TextView textView1 = new TextView(this);
                        textView1.setTextSize(17);
                        textView1.setTextColor(Color.BLACK);
                        textView1.setText(String.valueOf(i) + ":");

                        TextView textView2 = new TextView(this);
                        textView2.setTextSize(17);
                        textView2.setTextColor(Color.BLACK);
                        textView2.setText(time.getString(time.getColumnIndex(DBHelper.KEY_RECEPTIONS_TIME)));

                        TextView textView3 = new TextView(this);
                        textView3.setTextSize(17);
                        textView3.setTextColor(Color.BLACK);
                        textView3.setText(time.getString(time.getColumnIndex(DBHelper.KEY_RECEPTIONS_COUNT)));

                        TableRow tableRow = new TableRow(this);

                        tableRow.addView(textView1);
                        tableRow.addView(textView2);
                        tableRow.addView(textView3);

                        tableLayout.addView(tableRow);

                    } catch (Exception e) {
                        alert(e.getMessage());
                    }
                    i++;
                } while (time.moveToNext());
                time.close();
            }
            //Динамически создаем кнопки
            int id = 0;

            //Кнопка "Удалить"
            Button delete_button = new Button(this);
            delete_button.setId(id);
            delete_button.setText("Удалить");
            delete_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    AlertDialog.Builder delete_dialog = new AlertDialog.Builder(InfoCourseActivity.this);
                    delete_dialog.setTitle("Удалить")
                            .setMessage("Вы уверены?")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setCancelable(false);
                    delete_dialog.setPositiveButton("Да",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    /*try {
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
*/
                                        //Удаление из бд
                                        database.delete(DBHelper.TABLE_COURSES, "_id = ?",
                                                new String[]{course_id});
                                        database.delete(DBHelper.TABLE_RECEPTIONS, "courseid = ?",
                                                new String[]{course_id});

                      /*              } catch (Exception e) {
                                        alert(e.getMessage());
                                    }
                             */       //Возвращаемся на главный экран
                                    Intent intent = new Intent(InfoCourseActivity.this, ListOfCoursesActivity.class);
                                    startActivity(intent);
                                }
                            });
                    delete_dialog.setNeutralButton("Отменить",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });


                    AlertDialog alert = delete_dialog.create();
                    alert.show();
                }
            });
            tableLayout.addView(delete_button);

            //Кнопка "Изменить" ДЛЯ ЛЕНИ
            Button change_button = new Button(this);
            change_button.setId(id++);
            change_button.setText("Изменить");
            change_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //открываем страницу редактирования
                    Intent intent = new Intent(InfoCourseActivity.this, CourseEditActivity.class);
                    intent.putExtra("course_id",course_ID);
                    startActivity(intent);
                }
            });
            tableLayout.addView(change_button);




        }

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

}


