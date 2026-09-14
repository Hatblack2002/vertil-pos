package com.vertil.pos.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.vertil.pos.data.entity.SaleItemEntity;
import java.lang.Class;
import java.lang.Exception;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SaleItemDao_Impl implements SaleItemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SaleItemEntity> __insertionAdapterOfSaleItemEntity;

  public SaleItemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSaleItemEntity = new EntityInsertionAdapter<SaleItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sale_items` (`id`,`saleId`,`productId`,`productName`,`barcode`,`quantity`,`unitPriceCents`,`discountCents`,`subtotalCents`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SaleItemEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getSaleId());
        statement.bindLong(3, entity.getProductId());
        statement.bindString(4, entity.getProductName());
        if (entity.getBarcode() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBarcode());
        }
        statement.bindDouble(6, entity.getQuantity());
        statement.bindLong(7, entity.getUnitPriceCents());
        statement.bindLong(8, entity.getDiscountCents());
        statement.bindLong(9, entity.getSubtotalCents());
      }
    };
  }

  @Override
  public Object insertAll(final List<SaleItemEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSaleItemEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getBySale(final long saleId,
      final Continuation<? super List<SaleItemEntity>> $completion) {
    final String _sql = "SELECT * FROM sale_items WHERE saleId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, saleId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SaleItemEntity>>() {
      @Override
      @NonNull
      public List<SaleItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSaleId = CursorUtil.getColumnIndexOrThrow(_cursor, "saleId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfUnitPriceCents = CursorUtil.getColumnIndexOrThrow(_cursor, "unitPriceCents");
          final int _cursorIndexOfDiscountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "discountCents");
          final int _cursorIndexOfSubtotalCents = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotalCents");
          final List<SaleItemEntity> _result = new ArrayList<SaleItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SaleItemEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpSaleId;
            _tmpSaleId = _cursor.getLong(_cursorIndexOfSaleId);
            final long _tmpProductId;
            _tmpProductId = _cursor.getLong(_cursorIndexOfProductId);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpBarcode;
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _tmpBarcode = null;
            } else {
              _tmpBarcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final double _tmpQuantity;
            _tmpQuantity = _cursor.getDouble(_cursorIndexOfQuantity);
            final long _tmpUnitPriceCents;
            _tmpUnitPriceCents = _cursor.getLong(_cursorIndexOfUnitPriceCents);
            final long _tmpDiscountCents;
            _tmpDiscountCents = _cursor.getLong(_cursorIndexOfDiscountCents);
            final long _tmpSubtotalCents;
            _tmpSubtotalCents = _cursor.getLong(_cursorIndexOfSubtotalCents);
            _item = new SaleItemEntity(_tmpId,_tmpSaleId,_tmpProductId,_tmpProductName,_tmpBarcode,_tmpQuantity,_tmpUnitPriceCents,_tmpDiscountCents,_tmpSubtotalCents);
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
  public Object topProductsByQty(final long start, final long end, final int limit,
      final Continuation<? super List<ProductQty>> $completion) {
    final String _sql = "SELECT productId, SUM(quantity) as qty FROM sale_items WHERE saleId IN (SELECT id FROM sales WHERE status='COMPLETED' AND createdAt BETWEEN ? AND ?) GROUP BY productId ORDER BY qty DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    _argIndex = 3;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ProductQty>>() {
      @Override
      @NonNull
      public List<ProductQty> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfProductId = 0;
          final int _cursorIndexOfQty = 1;
          final List<ProductQty> _result = new ArrayList<ProductQty>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductQty _item;
            final long _tmpProductId;
            _tmpProductId = _cursor.getLong(_cursorIndexOfProductId);
            final double _tmpQty;
            _tmpQty = _cursor.getDouble(_cursorIndexOfQty);
            _item = new ProductQty(_tmpProductId,_tmpQty);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
