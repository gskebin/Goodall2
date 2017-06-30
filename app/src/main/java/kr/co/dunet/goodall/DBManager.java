package kr.co.dunet.goodall;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DBManager extends SQLiteOpenHelper {
	private static final String DB = "Dunetchat.db";
	private static final String MEMBER_TABLE = "member";
	private static final String USER_TABLE = "chat_user";
	private static final String CHAT_TABLE = "chat";
	private static final String ROOM_TABLE = "room";
	private static final int DB_VERSION = 1;
	
	public Context mContext = null;
	
	private static DBManager mDbManager = null;
	
	public static DBManager getInstance(Context context) {
		if (mDbManager == null) {
			mDbManager = new DBManager(context, DB, null, DB_VERSION);
		}
		
		return mDbManager;
	}
	
	private DBManager(Context context, String dbName, CursorFactory factory,
			int version) {
		super(context, dbName, factory, version);
		
		mContext = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + USER_TABLE
				+ "( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ " id TEXT, lastdate TEXT, " + " nickname TEXT );");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + MEMBER_TABLE
				+ "( id TEXT, nickname TEXT, type INTERGER );");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + CHAT_TABLE
				+ "( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + " rid TEXT, "
				+ " id TEXT, " + " room_code TEXT, " + " nickname TEXT, "
				+ " like INTEGER, " + " message TEXT, " + " reg_date TEXT, "
				+ " msg_type TEXT );");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + ROOM_TABLE
				+ "( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ " room_code TEXT, " + " room_name TEXT, " + " admin TEXT, "
				+ " room_password TEXT, " + " reg_date TEXT, "
				+ " mod_date TEXT, " + " no_read INTERGER NOT NULL DEFAULT 0 );");
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		System.out.println(oldVersion);
		/*
		 * switch (oldVersion) { case 1: try { db.beginTransaction();
		 * db.execSQL("ALTER TABLE " + MEMBER_TABLE + " ADD COLUMN " +
		 * " type Integer"); db.setTransactionSuccessful(); } catch
		 * (IllegalStateException e) { e.printStackTrace(); } finally {
		 * db.endTransaction(); } break; } if (oldVersion <= 3) {
		 * db.execSQL("CREATE TABLE IF NOT EXISTS " + USER_TABLE +
		 * "( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
		 * " id TEXT, lastdate TEXT );"); try { db.beginTransaction();
		 * db.execSQL("ALTER TABLE " + USER_TABLE + " ADD COLUMN " +
		 * " nickname TEXT"); db.setTransactionSuccessful(); } catch
		 * (IllegalStateException e) { e.printStackTrace(); } finally {
		 * db.endTransaction(); } } if (oldVersion <= 4) { try {
		 * db.beginTransaction(); db.execSQL("ALTER TABLE " + ROOM_TABLE +
		 * " ADD COLUMN " + " room_password TEXT");
		 * db.setTransactionSuccessful(); } catch (IllegalStateException e) {
		 * e.printStackTrace(); } finally { db.endTransaction(); } } if
		 * (oldVersion < 6) { try { db.beginTransaction();
		 * db.execSQL("ALTER TABLE " + ROOM_TABLE + " ADD COLUMN " +
		 * " room_password TEXT"); db.setTransactionSuccessful(); } catch
		 * (IllegalStateException e) { e.printStackTrace(); } finally {
		 * db.endTransaction(); } }
		 */
	}
	
	/**
	 * 로그인 정보 입력
	 * 
	 * @param addRowValue
	 * @return long
	 */
	public long insertMember(ContentValues addRowValue) {
		return getWritableDatabase().insert(MEMBER_TABLE, null, addRowValue);
	}
	
	/**
	 * 로그인 정보 삭제 (로그아웃)
	 * 
	 * @param whereClause
	 * @param whereArgs
	 * @return int
	 */
	public int deleteMember(String whereClause, String[] whereArgs) {
		return getWritableDatabase().delete(MEMBER_TABLE, whereClause,
				whereArgs);
	}
	
	public int updateMember(ContentValues updateRowValue, String whereClause,
			String[] whereArgs) {
		return getWritableDatabase().update(MEMBER_TABLE, updateRowValue,
				whereClause, whereArgs);
	}
	
	/**
	 * 로그인 정보
	 * 
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return Cursor
	 */
	public Cursor selectMember(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		return getReadableDatabase().query(MEMBER_TABLE, columns, selection,
				selectionArgs, groupBy, having, orderBy, "1");
	}
	
	public long insertUser(ContentValues addRowValue) {
		return getWritableDatabase().insert(USER_TABLE, null, addRowValue);
	}
	
	public int updateUser(ContentValues updateRowValue, String whereClause,
			String[] whereArgs) {
		return getWritableDatabase().update(USER_TABLE, updateRowValue,
				whereClause, whereArgs);
	}
	
	public Cursor selectUser(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		return getReadableDatabase().query(USER_TABLE, columns, selection,
				selectionArgs, groupBy, having, orderBy, "1");
	}
	
	public int deleteUser(String whereClause, String[] whereArgs) {
		return getWritableDatabase().delete(USER_TABLE, whereClause, whereArgs);
	}
	
	public long insertChat(ContentValues addRowValue) {
		return getWritableDatabase().insert(CHAT_TABLE, null, addRowValue);
	}
	
	public int updateChat(ContentValues updateRowValue, String whereClause,
			String[] whereArgs) {
		return getWritableDatabase().update(CHAT_TABLE, updateRowValue,
				whereClause, whereArgs);
	}
	
	public int deleteChat(String whereClause, String[] whereArgs) {
		return getWritableDatabase().delete(CHAT_TABLE, whereClause, whereArgs);
	}
	
	public Cursor selectChat(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		return getReadableDatabase().query(CHAT_TABLE, columns, selection,
				selectionArgs, groupBy, having, orderBy, limit);
	}
	
	public long insertRoom(ContentValues addRowValue) {
		return getWritableDatabase().insert(ROOM_TABLE, null, addRowValue);
	}
	
	public Cursor selectRoom(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		return getReadableDatabase().query(ROOM_TABLE, columns, selection,
				selectionArgs, groupBy, having, orderBy);
	}
	
	public int deleteRoom(String whereClause, String[] whereArgs) {
		return getWritableDatabase().delete(ROOM_TABLE, whereClause, whereArgs);
	}
	
	public int updateRoom(ContentValues updateRowValue, String whereClause,
			String[] whereArgs) {
		return getWritableDatabase().update(ROOM_TABLE, updateRowValue,
				whereClause, whereArgs);
	}
}