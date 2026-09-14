package com.vertil.pos.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.vertil.pos.data.entity.CashMovementEntity;
import com.vertil.pos.data.entity.CashSessionEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CashDao_Impl implements CashDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CashSessionEntity> __insertionAdapterOfCashSessionEntity;

  private final EntityInsertionAdapter<CashMovementEntity> __insertionAdapterOfCashMovementEntity;

  private final EntityDeletionOrUpdateAdapter<CashSessionEntity> __updateAdapterOfCashSessionEntity;

  public CashDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCashSessionEntity = new EntityInsertionAdapter<CashSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cash_sessions` (`id`,`userId`,`openingBalanceCents`,`closingBalanceCents`,`expectedBalanceCents`,`differenceCents`,`openTime`,`closeTime`,`status`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CashSessionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getUserId());
        statement.bindLong(3, entity.getOpeningBalanceCents());
        if (entity.getClosingBalanceCents() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getClosingBalanceCents());
        }
        if (entity.getExpectedBalanceCents() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getExpectedBalanceCents());
        }
        if (entity.getDifferenceCents() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getDifferenceCents());
        }
        statement.bindLong(7, entity.getOpenTime());
        if (entity.getCloseTime() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getCloseTime());
        }
        statement.bindString(9, entity.getStatus());
      }
    };
    this.__insertionAdapterOfCashMovementEntity = new EntityInsertionAdapter<CashMovementEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cash_movements` (`id`,`cashSessionId`,`type`,`amountCents`,`reason`,`saleId`,`userId`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CashMovementEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCashSessionId());
        statement.bindString(3, entity.getType());
        statement.bindLong(4, entity.getAmountCents());
        if (entity.getReason() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getReason());
        }
        if (entity.getSaleId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getSaleId());
        }
        statement.bindLong(7, entity.getUserId());
        statement.bindLong(8, entity.getCreatedAt());
      }
    };
    this.__updateAdapterOfCashSessionEntity = new EntityDeletionOrUpdateAdapter<CashSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `cash_sessions` SET `id` = ?,`userId` = ?,`openingBalanceCents` = ?,`closingBalanceCents` = ?,`expectedBalanceCents` = ?,`differenceCents` = ?,`openTime` = ?,`closeTime` = ?,`status` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CashSessionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getUserId());
        statement.bindLong(3, entity.getOpeningBalanceCents());
        if (entity.getClosingBalanceCents() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getClosingBalanceCents());
        }
        if (entity.getExpectedBalanceCents() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getExpectedBalanceCents());
        }
        if (entity.getDifferenceCents() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getDifferenceCents());
        }
        statement.bindLong(7, entity.getOpenTime());
        if (entity.getCloseTime() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getCloseTime());
        }
        statement.bindString(9, entity.getStatus());
        statement.bindLong(10, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final CashSessionEntity s, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCashSessionEntity.insertAndReturnId(s);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMovement(final CashMovementEntity m,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCashMovementEntity.insertAndReturnId(m);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final CashSessionEntity s, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCashSessionEntity.handle(s);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CashSessionEntity>> observeRecentSessions() {
    final String _sql = "SELECT * FROM cash_sessions ORDER BY openTime DESC LIMIT 50";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cash_sessions"}, new Callable<List<CashSessionEntity>>() {
      @Override
      @NonNull
      public List<CashSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfOpeningBalanceCents = CursorUtil.getColumnIndexOrThrow(_cursor, "openingBalanceCents");
          final int _cursorIndexOfClosingBalanceCents = CursorUtil.getColumnIndexOrThrow(_cursor, "closingBalanceCents");
          final int _cursorIndexOfExpectedBalanceCents = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedBalanceCents");
          final int _cursorIndexOfDifferenceCents = CursorUtil.getColumnIndexOrThrow(_cursor, "differenceCents");
          final int _cursorIndexOfOpenTime = CursorUtil.getColumnIndexOrThrow(_cursor, "openTime");
          final int _cursorIndexOfCloseTime = CursorUtil.getColumnIndexOrThrow(_cursor, "closeTime");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<CashSessionEntity> _result = new ArrayList<CashSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CashSessionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final long _tmpOpeningBalanceCents;
            _tmpOpeningBalanceCents = _cursor.getLong(_cursorIndexOfOpeningBalanceCents);
            final Long _tmpClosingBalanceCents;
            if (_cursor.isNull(_cursorIndexOfClosingBalanceCents)) {
              _tmpClosingBalanceCents = null;
            } else {
              _tmpClosingBalanceCents = _cursor.getLong(_cursorIndexOfClosingBalanceCents);
            }
            final Long _tmpExpectedBalanceCents;
            if (_cursor.isNull(_cursorIndexOfExpectedBalanceCents)) {
              _tmpExpectedBalanceCents = null;
            } else {
              _tmpExpectedBalanceCents = _cursor.getLong(_cursorIndexOfExpectedBalanceCents);
            }
            final Long _tmpDifferenceCents;
            if (_cursor.isNull(_cursorIndexOfDifferenceCents)) {
              _tmpDifferenceCents = null;
            } else {
              _tmpDifferenceCents = _cursor.getLong(_cursorIndexOfDifferenceCents);
            }
            final long _tmpOpenTime;
            _tmpOpenTime = _cursor.getLong(_cursorIndexOfOpenTime);
            final Long _tmpCloseTime;
            if (_cursor.isNull(_cursorIndexOfCloseTime)) {
              _tmpCloseTime = null;
            } else {
              _tmpCloseTime = _cursor.getLong(_cursorIndexOfCloseTime);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _item = new CashSessionEntity(_tmpId,_tmpUserId,_tmpOpeningBalanceCents,_tmpClosingBalanceCents,_tmpExpectedBalanceCents,_tmpDifferenceCents,_tmpOpenTime,_tmpCloseTime,_tmpStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getOpenSession(final Continuation<? super CashSessionEntity> $completion) {
    final String _sql = "SELECT * FROM cash_sessions WHERE status = 'OPEN' LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CashSessionEntity>() {
      @Override
      @Nullable
      public CashSessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfOpeningBalanceCents = CursorUtil.getColumnIndexOrThrow(_cursor, "openingBalanceCents");
          final int _cursorIndexOfClosingBalanceCents = CursorUtil.getColumnIndexOrThrow(_cursor, "closingBalanceCents");
          final int _cursorIndexOfExpectedBalanceCents = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedBalanceCents");
          final int _cursorIndexOfDifferenceCents = CursorUtil.getColumnIndexOrThrow(_cursor, "differenceCents");
          final int _cursorIndexOfOpenTime = CursorUtil.getColumnIndexOrThrow(_cursor, "openTime");
          final int _cursorIndexOfCloseTime = CursorUtil.getColumnIndexOrThrow(_cursor, "closeTime");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final CashSessionEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final long _tmpOpeningBalanceCents;
            _tmpOpeningBalanceCents = _cursor.getLong(_cursorIndexOfOpeningBalanceCents);
            final Long _tmpClosingBalanceCents;
            if (_cursor.isNull(_cursorIndexOfClosingBalanceCents)) {
              _tmpClosingBalanceCents = null;
            } else {
              _tmpClosingBalanceCents = _cursor.getLong(_cursorIndexOfClosingBalanceCents);
            }
            final Long _tmpExpectedBalanceCents;
            if (_cursor.isNull(_cursorIndexOfExpectedBalanceCents)) {
              _tmpExpectedBalanceCents = null;
            } else {
              _tmpExpectedBalanceCents = _cursor.getLong(_cursorIndexOfExpectedBalanceCents);
            }
            final Long _tmpDifferenceCents;
            if (_cursor.isNull(_cursorIndexOfDifferenceCents)) {
              _tmpDifferenceCents = null;
            } else {
              _tmpDifferenceCents = _cursor.getLong(_cursorIndexOfDifferenceCents);
            }
            final long _tmpOpenTime;
            _tmpOpenTime = _cursor.getLong(_cursorIndexOfOpenTime);
            final Long _tmpCloseTime;
            if (_cursor.isNull(_cursorIndexOfCloseTime)) {
              _tmpCloseTime = null;
            } else {
              _tmpCloseTime = _cursor.getLong(_cursorIndexOfCloseTime);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _result = new CashSessionEntity(_tmpId,_tmpUserId,_tmpOpeningBalanceCents,_tmpClosingBalanceCents,_tmpExpectedBalanceCents,_tmpDifferenceCents,_tmpOpenTime,_tmpCloseTime,_tmpStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getById(final long id, final Continuation<? super CashSessionEntity> $completion) {
    final String _sql = "SELECT * FROM cash_sessions WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CashSessionEntity>() {
      @Override
      @Nullable
      public CashSessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfOpeningBalanceCents = CursorUtil.getColumnIndexOrThrow(_cursor, "openingBalanceCents");
          final int _cursorIndexOfClosingBalanceCents = CursorUtil.getColumnIndexOrThrow(_cursor, "closingBalanceCents");
          final int _cursorIndexOfExpectedBalanceCents = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedBalanceCents");
          final int _cursorIndexOfDifferenceCents = CursorUtil.getColumnIndexOrThrow(_cursor, "differenceCents");
          final int _cursorIndexOfOpenTime = CursorUtil.getColumnIndexOrThrow(_cursor, "openTime");
          final int _cursorIndexOfCloseTime = CursorUtil.getColumnIndexOrThrow(_cursor, "closeTime");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final CashSessionEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final long _tmpOpeningBalanceCents;
            _tmpOpeningBalanceCents = _cursor.getLong(_cursorIndexOfOpeningBalanceCents);
            final Long _tmpClosingBalanceCents;
            if (_cursor.isNull(_cursorIndexOfClosingBalanceCents)) {
              _tmpClosingBalanceCents = null;
            } else {
              _tmpClosingBalanceCents = _cursor.getLong(_cursorIndexOfClosingBalanceCents);
            }
            final Long _tmpExpectedBalanceCents;
            if (_cursor.isNull(_cursorIndexOfExpectedBalanceCents)) {
              _tmpExpectedBalanceCents = null;
            } else {
              _tmpExpectedBalanceCents = _cursor.getLong(_cursorIndexOfExpectedBalanceCents);
            }
            final Long _tmpDifferenceCents;
            if (_cursor.isNull(_cursorIndexOfDifferenceCents)) {
              _tmpDifferenceCents = null;
            } else {
              _tmpDifferenceCents = _cursor.getLong(_cursorIndexOfDifferenceCents);
            }
            final long _tmpOpenTime;
            _tmpOpenTime = _cursor.getLong(_cursorIndexOfOpenTime);
            final Long _tmpCloseTime;
            if (_cursor.isNull(_cursorIndexOfCloseTime)) {
              _tmpCloseTime = null;
            } else {
              _tmpCloseTime = _cursor.getLong(_cursorIndexOfCloseTime);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _result = new CashSessionEntity(_tmpId,_tmpUserId,_tmpOpeningBalanceCents,_tmpClosingBalanceCents,_tmpExpectedBalanceCents,_tmpDifferenceCents,_tmpOpenTime,_tmpCloseTime,_tmpStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CashMovementEntity>> observeMovements(final long sessionId) {
    final String _sql = "SELECT * FROM cash_movements WHERE cashSessionId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sessionId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cash_movements"}, new Callable<List<CashMovementEntity>>() {
      @Override
      @NonNull
      public List<CashMovementEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCashSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashSessionId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "amountCents");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfSaleId = CursorUtil.getColumnIndexOrThrow(_cursor, "saleId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<CashMovementEntity> _result = new ArrayList<CashMovementEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CashMovementEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCashSessionId;
            _tmpCashSessionId = _cursor.getLong(_cursorIndexOfCashSessionId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpReason;
            if (_cursor.isNull(_cursorIndexOfReason)) {
              _tmpReason = null;
            } else {
              _tmpReason = _cursor.getString(_cursorIndexOfReason);
            }
            final Long _tmpSaleId;
            if (_cursor.isNull(_cursorIndexOfSaleId)) {
              _tmpSaleId = null;
            } else {
              _tmpSaleId = _cursor.getLong(_cursorIndexOfSaleId);
            }
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new CashMovementEntity(_tmpId,_tmpCashSessionId,_tmpType,_tmpAmountCents,_tmpReason,_tmpSaleId,_tmpUserId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getMovements(final long sessionId,
      final Continuation<? super List<CashMovementEntity>> $completion) {
    final String _sql = "SELECT * FROM cash_movements WHERE cashSessionId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CashMovementEntity>>() {
      @Override
      @NonNull
      public List<CashMovementEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCashSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashSessionId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "amountCents");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfSaleId = CursorUtil.getColumnIndexOrThrow(_cursor, "saleId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<CashMovementEntity> _result = new ArrayList<CashMovementEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CashMovementEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCashSessionId;
            _tmpCashSessionId = _cursor.getLong(_cursorIndexOfCashSessionId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpReason;
            if (_cursor.isNull(_cursorIndexOfReason)) {
              _tmpReason = null;
            } else {
              _tmpReason = _cursor.getString(_cursorIndexOfReason);
            }
            final Long _tmpSaleId;
            if (_cursor.isNull(_cursorIndexOfSaleId)) {
              _tmpSaleId = null;
            } else {
              _tmpSaleId = _cursor.getLong(_cursorIndexOfSaleId);
            }
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new CashMovementEntity(_tmpId,_tmpCashSessionId,_tmpType,_tmpAmountCents,_tmpReason,_tmpSaleId,_tmpUserId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object sumMovements(final long sessionId, final Continuation<? super Long> $completion) {
    final String _sql = "SELECT COALESCE(SUM(amountCents), 0) FROM cash_movements WHERE cashSessionId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
