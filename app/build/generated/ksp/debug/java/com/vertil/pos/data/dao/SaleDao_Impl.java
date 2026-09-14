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
import com.vertil.pos.data.entity.SaleEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class SaleDao_Impl implements SaleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SaleEntity> __insertionAdapterOfSaleEntity;

  private final EntityDeletionOrUpdateAdapter<SaleEntity> __updateAdapterOfSaleEntity;

  public SaleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSaleEntity = new EntityInsertionAdapter<SaleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sales` (`id`,`number`,`userId`,`customerId`,`cashSessionId`,`subtotalCents`,`discountCents`,`taxCents`,`totalCents`,`paymentMethod`,`receivedCents`,`changeCents`,`status`,`notes`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SaleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNumber());
        statement.bindLong(3, entity.getUserId());
        if (entity.getCustomerId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getCustomerId());
        }
        if (entity.getCashSessionId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getCashSessionId());
        }
        statement.bindLong(6, entity.getSubtotalCents());
        statement.bindLong(7, entity.getDiscountCents());
        statement.bindLong(8, entity.getTaxCents());
        statement.bindLong(9, entity.getTotalCents());
        statement.bindString(10, entity.getPaymentMethod());
        statement.bindLong(11, entity.getReceivedCents());
        statement.bindLong(12, entity.getChangeCents());
        statement.bindString(13, entity.getStatus());
        if (entity.getNotes() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getNotes());
        }
        statement.bindLong(15, entity.getCreatedAt());
      }
    };
    this.__updateAdapterOfSaleEntity = new EntityDeletionOrUpdateAdapter<SaleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sales` SET `id` = ?,`number` = ?,`userId` = ?,`customerId` = ?,`cashSessionId` = ?,`subtotalCents` = ?,`discountCents` = ?,`taxCents` = ?,`totalCents` = ?,`paymentMethod` = ?,`receivedCents` = ?,`changeCents` = ?,`status` = ?,`notes` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SaleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNumber());
        statement.bindLong(3, entity.getUserId());
        if (entity.getCustomerId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getCustomerId());
        }
        if (entity.getCashSessionId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getCashSessionId());
        }
        statement.bindLong(6, entity.getSubtotalCents());
        statement.bindLong(7, entity.getDiscountCents());
        statement.bindLong(8, entity.getTaxCents());
        statement.bindLong(9, entity.getTotalCents());
        statement.bindString(10, entity.getPaymentMethod());
        statement.bindLong(11, entity.getReceivedCents());
        statement.bindLong(12, entity.getChangeCents());
        statement.bindString(13, entity.getStatus());
        if (entity.getNotes() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getNotes());
        }
        statement.bindLong(15, entity.getCreatedAt());
        statement.bindLong(16, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final SaleEntity s, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSaleEntity.insertAndReturnId(s);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final SaleEntity s, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSaleEntity.handle(s);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SaleEntity>> observeRecent() {
    final String _sql = "SELECT * FROM sales ORDER BY createdAt DESC LIMIT 200";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sales"}, new Callable<List<SaleEntity>>() {
      @Override
      @NonNull
      public List<SaleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "number");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfCashSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashSessionId");
          final int _cursorIndexOfSubtotalCents = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotalCents");
          final int _cursorIndexOfDiscountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "discountCents");
          final int _cursorIndexOfTaxCents = CursorUtil.getColumnIndexOrThrow(_cursor, "taxCents");
          final int _cursorIndexOfTotalCents = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCents");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfReceivedCents = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedCents");
          final int _cursorIndexOfChangeCents = CursorUtil.getColumnIndexOrThrow(_cursor, "changeCents");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<SaleEntity> _result = new ArrayList<SaleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SaleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNumber;
            _tmpNumber = _cursor.getString(_cursorIndexOfNumber);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final Long _tmpCustomerId;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomerId = null;
            } else {
              _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            }
            final Long _tmpCashSessionId;
            if (_cursor.isNull(_cursorIndexOfCashSessionId)) {
              _tmpCashSessionId = null;
            } else {
              _tmpCashSessionId = _cursor.getLong(_cursorIndexOfCashSessionId);
            }
            final long _tmpSubtotalCents;
            _tmpSubtotalCents = _cursor.getLong(_cursorIndexOfSubtotalCents);
            final long _tmpDiscountCents;
            _tmpDiscountCents = _cursor.getLong(_cursorIndexOfDiscountCents);
            final long _tmpTaxCents;
            _tmpTaxCents = _cursor.getLong(_cursorIndexOfTaxCents);
            final long _tmpTotalCents;
            _tmpTotalCents = _cursor.getLong(_cursorIndexOfTotalCents);
            final String _tmpPaymentMethod;
            _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
            final long _tmpReceivedCents;
            _tmpReceivedCents = _cursor.getLong(_cursorIndexOfReceivedCents);
            final long _tmpChangeCents;
            _tmpChangeCents = _cursor.getLong(_cursorIndexOfChangeCents);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new SaleEntity(_tmpId,_tmpNumber,_tmpUserId,_tmpCustomerId,_tmpCashSessionId,_tmpSubtotalCents,_tmpDiscountCents,_tmpTaxCents,_tmpTotalCents,_tmpPaymentMethod,_tmpReceivedCents,_tmpChangeCents,_tmpStatus,_tmpNotes,_tmpCreatedAt);
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
  public Object getById(final long id, final Continuation<? super SaleEntity> $completion) {
    final String _sql = "SELECT * FROM sales WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SaleEntity>() {
      @Override
      @Nullable
      public SaleEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "number");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfCashSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashSessionId");
          final int _cursorIndexOfSubtotalCents = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotalCents");
          final int _cursorIndexOfDiscountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "discountCents");
          final int _cursorIndexOfTaxCents = CursorUtil.getColumnIndexOrThrow(_cursor, "taxCents");
          final int _cursorIndexOfTotalCents = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCents");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfReceivedCents = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedCents");
          final int _cursorIndexOfChangeCents = CursorUtil.getColumnIndexOrThrow(_cursor, "changeCents");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final SaleEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNumber;
            _tmpNumber = _cursor.getString(_cursorIndexOfNumber);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final Long _tmpCustomerId;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomerId = null;
            } else {
              _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            }
            final Long _tmpCashSessionId;
            if (_cursor.isNull(_cursorIndexOfCashSessionId)) {
              _tmpCashSessionId = null;
            } else {
              _tmpCashSessionId = _cursor.getLong(_cursorIndexOfCashSessionId);
            }
            final long _tmpSubtotalCents;
            _tmpSubtotalCents = _cursor.getLong(_cursorIndexOfSubtotalCents);
            final long _tmpDiscountCents;
            _tmpDiscountCents = _cursor.getLong(_cursorIndexOfDiscountCents);
            final long _tmpTaxCents;
            _tmpTaxCents = _cursor.getLong(_cursorIndexOfTaxCents);
            final long _tmpTotalCents;
            _tmpTotalCents = _cursor.getLong(_cursorIndexOfTotalCents);
            final String _tmpPaymentMethod;
            _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
            final long _tmpReceivedCents;
            _tmpReceivedCents = _cursor.getLong(_cursorIndexOfReceivedCents);
            final long _tmpChangeCents;
            _tmpChangeCents = _cursor.getLong(_cursorIndexOfChangeCents);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new SaleEntity(_tmpId,_tmpNumber,_tmpUserId,_tmpCustomerId,_tmpCashSessionId,_tmpSubtotalCents,_tmpDiscountCents,_tmpTaxCents,_tmpTotalCents,_tmpPaymentMethod,_tmpReceivedCents,_tmpChangeCents,_tmpStatus,_tmpNotes,_tmpCreatedAt);
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
  public Object getByNumber(final String number,
      final Continuation<? super SaleEntity> $completion) {
    final String _sql = "SELECT * FROM sales WHERE number = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, number);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SaleEntity>() {
      @Override
      @Nullable
      public SaleEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "number");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfCashSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashSessionId");
          final int _cursorIndexOfSubtotalCents = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotalCents");
          final int _cursorIndexOfDiscountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "discountCents");
          final int _cursorIndexOfTaxCents = CursorUtil.getColumnIndexOrThrow(_cursor, "taxCents");
          final int _cursorIndexOfTotalCents = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCents");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfReceivedCents = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedCents");
          final int _cursorIndexOfChangeCents = CursorUtil.getColumnIndexOrThrow(_cursor, "changeCents");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final SaleEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNumber;
            _tmpNumber = _cursor.getString(_cursorIndexOfNumber);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final Long _tmpCustomerId;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomerId = null;
            } else {
              _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            }
            final Long _tmpCashSessionId;
            if (_cursor.isNull(_cursorIndexOfCashSessionId)) {
              _tmpCashSessionId = null;
            } else {
              _tmpCashSessionId = _cursor.getLong(_cursorIndexOfCashSessionId);
            }
            final long _tmpSubtotalCents;
            _tmpSubtotalCents = _cursor.getLong(_cursorIndexOfSubtotalCents);
            final long _tmpDiscountCents;
            _tmpDiscountCents = _cursor.getLong(_cursorIndexOfDiscountCents);
            final long _tmpTaxCents;
            _tmpTaxCents = _cursor.getLong(_cursorIndexOfTaxCents);
            final long _tmpTotalCents;
            _tmpTotalCents = _cursor.getLong(_cursorIndexOfTotalCents);
            final String _tmpPaymentMethod;
            _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
            final long _tmpReceivedCents;
            _tmpReceivedCents = _cursor.getLong(_cursorIndexOfReceivedCents);
            final long _tmpChangeCents;
            _tmpChangeCents = _cursor.getLong(_cursorIndexOfChangeCents);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new SaleEntity(_tmpId,_tmpNumber,_tmpUserId,_tmpCustomerId,_tmpCashSessionId,_tmpSubtotalCents,_tmpDiscountCents,_tmpTaxCents,_tmpTotalCents,_tmpPaymentMethod,_tmpReceivedCents,_tmpChangeCents,_tmpStatus,_tmpNotes,_tmpCreatedAt);
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
  public Object getByDateRange(final long start, final long end,
      final Continuation<? super List<SaleEntity>> $completion) {
    final String _sql = "SELECT * FROM sales WHERE createdAt BETWEEN ? AND ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SaleEntity>>() {
      @Override
      @NonNull
      public List<SaleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "number");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfCashSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashSessionId");
          final int _cursorIndexOfSubtotalCents = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotalCents");
          final int _cursorIndexOfDiscountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "discountCents");
          final int _cursorIndexOfTaxCents = CursorUtil.getColumnIndexOrThrow(_cursor, "taxCents");
          final int _cursorIndexOfTotalCents = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCents");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfReceivedCents = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedCents");
          final int _cursorIndexOfChangeCents = CursorUtil.getColumnIndexOrThrow(_cursor, "changeCents");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<SaleEntity> _result = new ArrayList<SaleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SaleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNumber;
            _tmpNumber = _cursor.getString(_cursorIndexOfNumber);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final Long _tmpCustomerId;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomerId = null;
            } else {
              _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            }
            final Long _tmpCashSessionId;
            if (_cursor.isNull(_cursorIndexOfCashSessionId)) {
              _tmpCashSessionId = null;
            } else {
              _tmpCashSessionId = _cursor.getLong(_cursorIndexOfCashSessionId);
            }
            final long _tmpSubtotalCents;
            _tmpSubtotalCents = _cursor.getLong(_cursorIndexOfSubtotalCents);
            final long _tmpDiscountCents;
            _tmpDiscountCents = _cursor.getLong(_cursorIndexOfDiscountCents);
            final long _tmpTaxCents;
            _tmpTaxCents = _cursor.getLong(_cursorIndexOfTaxCents);
            final long _tmpTotalCents;
            _tmpTotalCents = _cursor.getLong(_cursorIndexOfTotalCents);
            final String _tmpPaymentMethod;
            _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
            final long _tmpReceivedCents;
            _tmpReceivedCents = _cursor.getLong(_cursorIndexOfReceivedCents);
            final long _tmpChangeCents;
            _tmpChangeCents = _cursor.getLong(_cursorIndexOfChangeCents);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new SaleEntity(_tmpId,_tmpNumber,_tmpUserId,_tmpCustomerId,_tmpCashSessionId,_tmpSubtotalCents,_tmpDiscountCents,_tmpTaxCents,_tmpTotalCents,_tmpPaymentMethod,_tmpReceivedCents,_tmpChangeCents,_tmpStatus,_tmpNotes,_tmpCreatedAt);
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
  public Object countCompleted(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM sales WHERE status = 'COMPLETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Object sumTotalCentsBetween(final long start, final long end,
      final Continuation<? super Long> $completion) {
    final String _sql = "SELECT COALESCE(SUM(totalCents), 0) FROM sales WHERE status = 'COMPLETED' AND createdAt BETWEEN ? AND ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
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

  @Override
  public Object maxSaleNumber(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COALESCE(MAX(CAST(SUBSTR(number, 3) AS INTEGER)), 0) FROM sales";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
