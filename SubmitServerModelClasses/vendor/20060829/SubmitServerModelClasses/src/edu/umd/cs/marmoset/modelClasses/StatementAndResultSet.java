/*
 * Created on Aug 30, 2004
 */
package edu.umd.cs.marmoset.modelClasses;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A PreparedStatement and a ResultSet representing an query,
 * positioned at some row.  The reason this class exists is
 * to make it possible to return a ResultSet from a method,
 * and still ensure that both statement and result set are
 * closed when the query is done.
 * 
 * @author daveho
 */
public class StatementAndResultSet {
	private PreparedStatement statement;
	private ResultSet resultSet;
	
	/**
	 * Constructor.
	 * @param statement the PreparedStatement to be executed
	 */
	public StatementAndResultSet(PreparedStatement statement) {
		this.statement = statement;
	}
	
	/**
	 * Execute the statement.
	 * If successful, the ResultSet will be available, positioned
	 * just before the first row returned.
	 * 
	 * @throws SQLException
	 */
	public void execute() throws SQLException {
		resultSet = statement.executeQuery();
	}
	
	/**
	 * Close the statement.
	 * As a side-effect, this closes the ResultSet (if any).
	 */
	public void close() {
		try {
			statement.close();
		} catch (SQLException e) {
			// Ignored
		}
	}

	/**
	 * @return Returns the resultSet.
	 */
	public ResultSet getResultSet() {
		return resultSet;
	}
	
	/**
	 * @return Returns the statement.
	 */
	public PreparedStatement getStatement() {
		return statement;
	}
}
