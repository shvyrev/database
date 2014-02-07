package edu.stanford.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.easymock.IMocksControl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Unit tests for the Database and Sql implementation classes.
 *
 * @author garricko
 */
@RunWith(JUnit4.class)
public class DatabaseTest {
  @Test
  public void staticSqlToLong() throws Exception {
    Connection c = createNiceMock(Connection.class);
    PreparedStatement ps = createNiceMock(PreparedStatement.class);
    ResultSet rs = createNiceMock(ResultSet.class);

    expect(c.prepareStatement("select 1 from dual")).andReturn(ps);
    expect(ps.executeQuery()).andReturn(rs);
    expect(rs.next()).andReturn(true);
    expect(rs.getLong(1)).andReturn(1L);
    expect(rs.wasNull()).andReturn(false);

    replay(c, ps, rs);

    assertEquals(new Long(1), new DatabaseImpl(c).select("select 1 from dual").queryLong());

    verify(c, ps, rs);
  }

  @Test
  public void staticSqlToLongNoRows() throws Exception {
    Connection c = createNiceMock(Connection.class);
    PreparedStatement ps = createNiceMock(PreparedStatement.class);
    ResultSet rs = createNiceMock(ResultSet.class);

    expect(c.prepareStatement("select * from dual")).andReturn(ps);
    expect(ps.executeQuery()).andReturn(rs);
    expect(rs.next()).andReturn(false);

    replay(c, ps, rs);

    assertNull(new DatabaseImpl(c).select("select * from dual").queryLong());

    verify(c, ps, rs);
  }

  @Test
  public void staticSqlToLongNullValue() throws Exception {
    Connection c = createNiceMock(Connection.class);
    PreparedStatement ps = createNiceMock(PreparedStatement.class);
    ResultSet rs = createNiceMock(ResultSet.class);

    expect(c.prepareStatement("select null from dual")).andReturn(ps);
    expect(ps.executeQuery()).andReturn(rs);
    expect(rs.next()).andReturn(true);
    expect(rs.getLong(1)).andReturn(0L);
    expect(rs.wasNull()).andReturn(true);

    replay(c, ps, rs);

    assertNull(new DatabaseImpl(c).select("select null from dual").queryLong());

    verify(c, ps, rs);
  }

  @Test
  public void sqlArgLongPositional() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);
    PreparedStatement ps = control.createMock(PreparedStatement.class);
    ResultSet rs = control.createMock(ResultSet.class);

    expect(c.prepareStatement("select a from b where c=?")).andReturn(ps);
    ps.setObject(eq(1), eq(new Long(1)));
    expect(ps.executeQuery()).andReturn(rs);
    expect(rs.next()).andReturn(false);
    rs.close();
    ps.close();

    control.replay();

    assertNull(new DatabaseImpl(c).select("select a from b where c=?").argLong(1L).queryLong());

    control.verify();
  }

  @Test
  public void sqlArgLongNull() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);
    PreparedStatement ps = control.createMock(PreparedStatement.class);
    ResultSet rs = control.createMock(ResultSet.class);

    expect(c.prepareStatement("select a from b where c=?")).andReturn(ps);
    ps.setNull(eq(1), eq(Types.NUMERIC));
    expect(ps.executeQuery()).andReturn(rs);
    expect(rs.next()).andReturn(false);
    rs.close();
    ps.close();

    control.replay();

    assertNull(new DatabaseImpl(c).select("select a from b where c=?").argLong(null).queryLong());

    control.verify();
  }

  @Test
  public void settingTimeout() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);
    PreparedStatement ps = control.createMock(PreparedStatement.class);
    ResultSet rs = control.createMock(ResultSet.class);

    expect(c.prepareStatement("select a from b")).andReturn(ps);
    ps.setQueryTimeout(21);
    expect(ps.executeQuery()).andReturn(rs);
    expect(rs.next()).andReturn(false);
    rs.close();
    ps.close();

    control.replay();

    assertNull(new DatabaseImpl(c).select("select a from b").withTimeoutSeconds(21).queryLong());

    control.verify();
  }

  @Test
  public void settingMaxRows() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);
    PreparedStatement ps = control.createMock(PreparedStatement.class);
    ResultSet rs = control.createMock(ResultSet.class);

    expect(c.prepareStatement("select a from b")).andReturn(ps);
    ps.setMaxRows(15);
    expect(ps.executeQuery()).andReturn(rs);
    expect(rs.next()).andReturn(false);
    rs.close();
    ps.close();

    control.replay();

    assertNull(new DatabaseImpl(c).select("select a from b").withMaxRows(15).queryLong());

    control.verify();
  }

  @Test
  public void sqlArgLongNamed() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);
    PreparedStatement ps = control.createMock(PreparedStatement.class);
    ResultSet rs = control.createMock(ResultSet.class);

    expect(c.prepareStatement("select ':a' from b where c=?")).andReturn(ps);
    ps.setObject(eq(1), eq(new Long(1)));
    expect(ps.executeQuery()).andReturn(rs);
    expect(rs.next()).andReturn(false);
    rs.close();
    ps.close();

    control.replay();

    assertNull(new DatabaseImpl(c).select("select '::a' from b where c=:c").argLong("c", 1L).queryLong());

    control.verify();
  }

  @Test
  public void missingPositionalParameter() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);
    PreparedStatement ps = control.createMock(PreparedStatement.class);

    expect(c.prepareStatement("select a from b where c=?")).andReturn(ps);
    expect(ps.executeQuery()).andThrow(new SQLException("Wrong number of parameters"));
    ps.close();

    control.replay();

    try {
      new DatabaseImpl(c).select("select a from b where c=?").queryLong();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("Error executing SQL: (wrong # args) query: select a from b where c=? args: []", e.getMessage());
    }

    control.verify();
  }

  @Test
  public void extraPositionalParameter() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);
    PreparedStatement ps = control.createMock(PreparedStatement.class);

    expect(c.prepareStatement("select a from b where c=?")).andReturn(ps);
    ps.setObject(eq(1), eq("hi"));
    ps.setObject(eq(2), eq(new Integer(1)));
    expect(ps.executeQuery()).andThrow(new SQLException("Wrong number of parameters"));
    ps.close();

    control.replay();

    try {
      new DatabaseImpl(c).select("select a from b where c=?").argString("hi").argInteger(1).queryLong();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("Error executing SQL: (wrong # args) query: select a from b where c=? args: [hi, 1]", e.getMessage());
    }

    control.verify();
  }

  @Test
  public void missingNamedParameter() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);
    PreparedStatement ps = control.createMock(PreparedStatement.class);

    expect(c.prepareStatement("select a from b where c=:x")).andReturn(ps);
    expect(ps.executeQuery()).andThrow(new SQLException("Wrong number of parameters"));
    ps.close();

    control.replay();

    try {
      new DatabaseImpl(c).select("select a from b where c=:x").queryLong();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("Error executing SQL: select a from b where c=:x|select a from b where c=:x", e.getMessage());
    }

    control.verify();
  }

  @Test
  public void missingNamedParameter2() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);

    control.replay();

    try {
      new DatabaseImpl(c).select("select a from b where c=:x and d=:y").argString("x", "hi").queryLong();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("The SQL requires parameter 'y' but no value was provided", e.getMessage());
    }

    control.verify();
  }

  @Test
  public void extraNamedParameter() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);

    control.replay();

    try {
      new DatabaseImpl(c).select("select a from b where c=:x").argString("x", "hi").argString("y", "bye").queryLong();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("These named parameters do not exist in the query: [y]", e.getMessage());
    }

    control.verify();
  }

  @Test
  public void mixedParameterTypes() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);

    control.replay();

    try {
      new DatabaseImpl(c).select("select a from b where c=:x").argString("y", "bye").argDate(null).queryLong();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("Use either positional or named query parameters, not both", e.getMessage());
    }

    // Reverse order of args should be the same
    try {
      new DatabaseImpl(c).select("select a from b where c=:x").argDate(null).argString("y", "bye").queryLong();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("Use either positional or named query parameters, not both", e.getMessage());
    }

    control.verify();
  }

  @Test
  public void cancelQuery() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);
    PreparedStatement ps = control.createMock(PreparedStatement.class);

    expect(c.prepareStatement("select a from b")).andReturn(ps);
    expect(ps.executeQuery()).andThrow(new SQLException("Cancelled", "cancel", 1013));
    ps.close();

    control.replay();

    try {
      new DatabaseImpl(c).select("select a from b").queryLong();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("Timeout of -1 seconds exceeded or user cancelled", e.getMessage());
    }

    control.verify();
  }

  @Test
  public void otherException() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);
    PreparedStatement ps = control.createMock(PreparedStatement.class);

    expect(c.prepareStatement("select a from b")).andReturn(ps);
    expect(ps.executeQuery()).andThrow(new RuntimeException("Oops"));
    ps.close();

    control.replay();

    try {
      new DatabaseImpl(c).select("select a from b").queryLong();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("Error executing SQL: select a from b|select a from b", e.getMessage());
    }

    control.verify();
  }

  @Test
  public void closeExceptions() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);
    PreparedStatement ps = control.createMock(PreparedStatement.class);
    ResultSet rs = control.createMock(ResultSet.class);

    expect(c.prepareStatement("select a from b")).andReturn(ps);
    expect(ps.executeQuery()).andReturn(rs);
    expect(rs.next()).andThrow(new RuntimeException("Primary"));
    rs.close();
    expectLastCall().andThrow(new RuntimeException("Oops1"));
    ps.close();
    expectLastCall().andThrow(new RuntimeException("Oops2"));

    control.replay();

    try {
      new DatabaseImpl(c).select("select a from b").queryLong();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("Error executing SQL: select a from b|select a from b", e.getMessage());
    }

    control.verify();
  }

  @Test
  public void transactionsNotAllowed() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);

    control.replay();

    try {
      new DatabaseImpl(c).commitNow();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("Calls to commitNow() are not allowed", e.getMessage());
    }

    try {
      new DatabaseImpl(c).rollbackNow();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("Calls to rollbackNow() are not allowed", e.getMessage());
    }

    control.verify();
  }

  @Test
  public void transactionCommitSuccess() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);

    c.commit();

    control.replay();

    new DatabaseImpl(c, true).commitNow();

    control.verify();
  }

  @Test
  public void transactionCommitFail() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);

    c.commit();
    expectLastCall().andThrow(new SQLException("Oops"));

    control.replay();

    try {
      new DatabaseImpl(c, true).commitNow();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("Unable to commit transaction", e.getMessage());
    }

    control.verify();
  }

  @Test
  public void transactionRollbackSuccess() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);

    c.rollback();

    control.replay();

    new DatabaseImpl(c, true).rollbackNow();

    control.verify();
  }

  @Test
  public void transactionRollbackFail() throws Exception {
    IMocksControl control = createStrictControl();

    Connection c = control.createMock(Connection.class);

    c.rollback();
    expectLastCall().andThrow(new SQLException("Oops"));

    control.replay();

    try {
      new DatabaseImpl(c, true).rollbackNow();
      fail("Should have thrown an exception");
    } catch (DatabaseException e) {
      assertEquals("Unable to rollback transaction", e.getMessage());
    }

    control.verify();
  }
}
