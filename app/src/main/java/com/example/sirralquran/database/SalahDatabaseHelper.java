package com.example.sirralquran.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.sirralquran.models.Prayer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * FIXED Database Helper - PREVENTS OVERWRITING user data on refresh
 */
public class SalahDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "SalahDatabaseHelper";
    private static final String DATABASE_NAME = "SalahTracker.db";
    private static final int DATABASE_VERSION = 4;  // ← UPDATED TO 4

    private static final String TABLE_PRAYERS = "prayers";

    // Columns
    private static final String COL_ID = "id";
    private static final String COL_DATE = "date";
    private static final String COL_PRAYER_NAME = "prayer_name";
    private static final String COL_PRAYER_NAME_ARABIC = "prayer_name_arabic";
    private static final String COL_PRAYER_TIME = "prayer_time";
    private static final String COL_IS_COMPLETED = "is_completed";
    private static final String COL_IS_QAZA = "is_qaza";
    private static final String COL_COMPLETED_AT = "completed_at";
    private static final String COL_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String COL_NOTIFICATION_OFFSET = "notification_offset_minutes";
    private static final String COL_OFFERED_TIME = "offered_time";
    private static final String COL_FIQH_METHOD = "fiqh_method";  // NEW

    private static SalahDatabaseHelper instance;

    public static synchronized SalahDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SalahDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private SalahDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_PRAYERS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_DATE + " TEXT NOT NULL, "
                + COL_PRAYER_NAME + " TEXT NOT NULL, "
                + COL_PRAYER_NAME_ARABIC + " TEXT, "
                + COL_PRAYER_TIME + " TEXT, "
                + COL_IS_COMPLETED + " INTEGER DEFAULT 0, "
                + COL_IS_QAZA + " INTEGER DEFAULT 0, "
                + COL_COMPLETED_AT + " TEXT, "
                + COL_NOTIFICATION_ENABLED + " INTEGER DEFAULT 1, "
                + COL_NOTIFICATION_OFFSET + " INTEGER DEFAULT 15, "
                + COL_OFFERED_TIME + " TEXT, "
                + COL_FIQH_METHOD + " INTEGER DEFAULT 1"
                + ")";

        db.execSQL(CREATE_TABLE);
        Log.d(TAG, "✅ Database created (v4) with fiqh_method column");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "🔄 Upgrading database from v" + oldVersion + " to v" + newVersion);

        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_PRAYERS + " ADD COLUMN " +
                        COL_NOTIFICATION_ENABLED + " INTEGER DEFAULT 1");
            } catch (Exception e) {
                Log.e(TAG, "Column exists: " + e.getMessage());
            }

            try {
                db.execSQL("ALTER TABLE " + TABLE_PRAYERS + " ADD COLUMN " +
                        COL_NOTIFICATION_OFFSET + " INTEGER DEFAULT 15");
            } catch (Exception e) {
                Log.e(TAG, "Column exists: " + e.getMessage());
            }
        }

        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_PRAYERS + " ADD COLUMN " +
                        COL_OFFERED_TIME + " TEXT");
            } catch (Exception e) {
                Log.e(TAG, "Column exists: " + e.getMessage());
            }
        }

        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_PRAYERS + " ADD COLUMN " +
                        COL_FIQH_METHOD + " INTEGER DEFAULT 1");
                Log.d(TAG, "✅ Added fiqh_method column");
            } catch (Exception e) {
                Log.e(TAG, "Column exists: " + e.getMessage());
            }
        }

        Log.d(TAG, "✅ Database upgraded to v" + newVersion);
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return sdf.format(new Date());
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        return sdf.format(new Date());
    }

    /**
     * CRITICAL FIX: Update ONLY prayer times from API (preserve user data)
     */
    public void updatePrayerTimesOnly(List<Prayer> newPrayers, int fiqhMethod) {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getTodayDate();

        Log.d(TAG, "🔄 Updating prayer times (preserving user data)...");

        for (Prayer newPrayer : newPrayers) {
            // Check if prayer exists
            Cursor cursor = db.query(
                    TABLE_PRAYERS,
                    null,  // Get all columns
                    COL_DATE + "=? AND " + COL_PRAYER_NAME + "=?",
                    new String[]{today, newPrayer.getName()},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                // PRAYER EXISTS - UPDATE ONLY TIME AND FIQH METHOD
                ContentValues values = new ContentValues();
                values.put(COL_PRAYER_TIME, newPrayer.getTime());
                values.put(COL_PRAYER_NAME_ARABIC, newPrayer.getNameArabic());
                values.put(COL_FIQH_METHOD, fiqhMethod);
                // DON'T UPDATE: completed, qaza, notification settings, offered_time

                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                db.update(TABLE_PRAYERS, values, COL_ID + "=?", new String[]{String.valueOf(id)});

                Log.d(TAG, "✅ Updated TIME ONLY for: " + newPrayer.getName() + " → " + newPrayer.getTime());
            } else {
                // PRAYER DOESN'T EXIST - INSERT NEW
                ContentValues values = new ContentValues();
                values.put(COL_DATE, today);
                values.put(COL_PRAYER_NAME, newPrayer.getName());
                values.put(COL_PRAYER_NAME_ARABIC, newPrayer.getNameArabic());
                values.put(COL_PRAYER_TIME, newPrayer.getTime());
                values.put(COL_IS_COMPLETED, 0);
                values.put(COL_IS_QAZA, 0);
                values.put(COL_NOTIFICATION_ENABLED, 1);
                values.put(COL_NOTIFICATION_OFFSET, 15);
                values.put(COL_FIQH_METHOD, fiqhMethod);

                db.insert(TABLE_PRAYERS, null, values);
                Log.d(TAG, "✅ Inserted NEW prayer: " + newPrayer.getName());
            }

            if (cursor != null) cursor.close();
        }

        db.close();
    }

    /**
     * CRITICAL FIX: Update ONLY user prayer status (completed, qaza, notification settings)
     */
    public void updatePrayerStatus(Prayer prayer) {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getTodayDate();

        Cursor cursor = db.query(
                TABLE_PRAYERS,
                new String[]{COL_ID},
                COL_DATE + "=? AND " + COL_PRAYER_NAME + "=?",
                new String[]{today, prayer.getName()},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(COL_IS_COMPLETED, prayer.isCompleted() ? 1 : 0);
            values.put(COL_IS_QAZA, prayer.isQaza() ? 1 : 0);
            values.put(COL_NOTIFICATION_ENABLED, prayer.hasNotification() ? 1 : 0);
            values.put(COL_NOTIFICATION_OFFSET, prayer.getNotificationOffset());
            values.put(COL_COMPLETED_AT, prayer.isCompleted() ?
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date()) : null);

            // Save offered time
            if (prayer.isCompleted() && prayer.getOfferedTime() == null) {
                prayer.setOfferedTime(getCurrentTime());
            }
            values.put(COL_OFFERED_TIME, prayer.getOfferedTime());

            int id = cursor.getInt(0);
            db.update(TABLE_PRAYERS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
            Log.d(TAG, "✅ Updated STATUS for: " + prayer.getName());
        } else {
            // Insert new if doesn't exist
            savePrayerFull(prayer);
        }

        if (cursor != null) cursor.close();
        db.close();
    }

    /**
     * FULL SAVE (used only for initial insert or complete overwrite)
     */
    private void savePrayerFull(Prayer prayer) {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getTodayDate();

        ContentValues values = new ContentValues();
        values.put(COL_DATE, today);
        values.put(COL_PRAYER_NAME, prayer.getName());
        values.put(COL_PRAYER_NAME_ARABIC, prayer.getNameArabic());
        values.put(COL_PRAYER_TIME, prayer.getTime());
        values.put(COL_IS_COMPLETED, prayer.isCompleted() ? 1 : 0);
        values.put(COL_IS_QAZA, prayer.isQaza() ? 1 : 0);
        values.put(COL_COMPLETED_AT, prayer.isCompleted() ?
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date()) : null);
        values.put(COL_NOTIFICATION_ENABLED, prayer.hasNotification() ? 1 : 0);
        values.put(COL_NOTIFICATION_OFFSET, prayer.getNotificationOffset());

        if (prayer.isCompleted() && prayer.getOfferedTime() == null) {
            prayer.setOfferedTime(getCurrentTime());
        }
        values.put(COL_OFFERED_TIME, prayer.getOfferedTime());

        db.insert(TABLE_PRAYERS, null, values);
        db.close();
    }

    /**
     * Get today's prayers
     */
    public List<Prayer> getTodayPrayers() {
        List<Prayer> prayers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getTodayDate();

        Cursor cursor = db.query(
                TABLE_PRAYERS,
                null,
                COL_DATE + "=?",
                new String[]{today},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Prayer prayer = new Prayer();
                prayer.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRAYER_NAME)));
                prayer.setNameArabic(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRAYER_NAME_ARABIC)));
                prayer.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRAYER_TIME)));
                prayer.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_COMPLETED)) == 1);
                prayer.setQaza(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_QAZA)) == 1);

                try {
                    prayer.setHasNotification(cursor.getInt(cursor.getColumnIndexOrThrow(COL_NOTIFICATION_ENABLED)) == 1);
                    prayer.setNotificationOffset(cursor.getInt(cursor.getColumnIndexOrThrow(COL_NOTIFICATION_OFFSET)));

                    int offeredTimeIndex = cursor.getColumnIndex(COL_OFFERED_TIME);
                    if (offeredTimeIndex >= 0 && !cursor.isNull(offeredTimeIndex)) {
                        prayer.setOfferedTime(cursor.getString(offeredTimeIndex));
                    }
                } catch (IllegalArgumentException e) {
                    prayer.setHasNotification(true);
                    prayer.setNotificationOffset(15);
                    prayer.setOfferedTime(null);
                }

                prayers.add(prayer);
            } while (cursor.moveToNext());
        }

        if (cursor != null) cursor.close();
        db.close();

        Log.d(TAG, "✅ Loaded " + prayers.size() + " prayers with ALL user data preserved");
        return prayers;
    }

    /**
     * Get stored Fiqh method for today's prayers
     */
    public int getStoredFiqhMethod() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getTodayDate();

        Cursor cursor = db.query(
                TABLE_PRAYERS,
                new String[]{COL_FIQH_METHOD},
                COL_DATE + "=?",
                new String[]{today},
                null, null, null,
                "1"  // LIMIT 1
        );

        int method = 1;  // Default: Karachi
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int methodIndex = cursor.getColumnIndex(COL_FIQH_METHOD);
                if (methodIndex >= 0 && !cursor.isNull(methodIndex)) {
                    method = cursor.getInt(methodIndex);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading fiqh method: " + e.getMessage());
            }
        }

        if (cursor != null) cursor.close();
        db.close();

        Log.d(TAG, "📖 Stored Fiqh method: " + method);
        return method;
    }

    public int getCompletedPrayersToday() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getTodayDate();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_PRAYERS +
                        " WHERE " + COL_DATE + "=? AND " + COL_IS_COMPLETED + "=1 AND " + COL_IS_QAZA + "=0",
                new String[]{today}
        );

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        if (cursor != null) cursor.close();
        db.close();
        return count;
    }

    public int getQazaPrayersToday() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getTodayDate();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_PRAYERS +
                        " WHERE " + COL_DATE + "=? AND " + COL_IS_QAZA + "=1",
                new String[]{today}
        );

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        if (cursor != null) cursor.close();
        db.close();
        return count;
    }

    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRAYERS, null, null);
        db.close();
        Log.d(TAG, "❌ All data deleted");
    }

    public void deleteOldData(int daysToKeep) {
        SQLiteDatabase db = this.getWritableDatabase();
        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60L * 60L * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String cutoffDate = sdf.format(new Date(cutoffTime));

        int deleted = db.delete(TABLE_PRAYERS, COL_DATE + " < ?", new String[]{cutoffDate});
        db.close();
        Log.d(TAG, "❌ Deleted " + deleted + " old records");
    }
}