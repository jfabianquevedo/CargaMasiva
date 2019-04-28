package jquevedo.Dao;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import jquevedo.model.balanceDao;

public class conexion {

	static DataSource conexion;

	static Connection con = null;

	public DataSource getConexion() {

		return conexion;
	}

	public void setConexion(DataSource conexion) {
		this.conexion = conexion;
	}

	public void getDatos() {
		try {
			String sql = "select * from balance";
			con = conexion.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString("id_balance"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void guardar(ArrayList<balanceDao> listaBalances) {
		for (balanceDao balanceDao : listaBalances) {
			String sql = "insert into balance (nombre) values(?)";
			try {
				con = conexion.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setString(1, balanceDao.getNombre());
				ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void guardarBatchTemplate(final ArrayList<balanceDao> listaBalance) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(conexion);
		String sql = "insert into balance (nombre) values(?)";
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			public void setValues(PreparedStatement ps, int i) throws SQLException {
				balanceDao balanceDao = listaBalance.get(i);
				ps.setString(1, balanceDao.getNombre());
			}

			public int getBatchSize() {
				return listaBalance.size();
			}
		});

	}

	public static ArrayList<balanceDao> guardarBatchSimpleInsert(ArrayList<balanceDao> listaBalance) {
		ArrayList<balanceDao> listaBalanceId = new ArrayList<balanceDao>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(conexion);
		SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("balance")
				.usingGeneratedKeyColumns("id_balance");
		for (balanceDao balanceDao : listaBalance) {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("nombre", balanceDao.getNombre());
			Number idBalance = jdbcInsert.executeAndReturnKey(parameters);
			balanceDao.setId_balance(idBalance.intValue());
			listaBalanceId.add(balanceDao);

		}
		return listaBalanceId;
	}

	public static void guardarPartidasPS(final ArrayList<balanceDao> listPartidas) throws Exception {

		String sql = "insert into partidas(id_balance,part_cod,pat_val) values(?,?,?)";
		con = conexion.getConnection();
		PreparedStatement ps = con.prepareStatement(sql);
		int counter = 0;
		for (balanceDao balance : listPartidas) {
			HashMap<String, BigDecimal> part = crearPartidasCargar(balance);
			for (String key : part.keySet()) {
				BigDecimal value = (BigDecimal) part.get(key);
				ps.setInt(1, balance.getId_balance());
				ps.setString(2, key);
				ps.setBigDecimal(3, value);
				ps.addBatch();
			}
			counter++;
		}
		if (counter == 1000) {
			ps.executeBatch();
			counter = 0;
		}
		if (counter > 0) {
			ps.executeBatch();
		}

	}

	public static void guardarPartidasBatch(final ArrayList<balanceDao> listPartidas) throws Exception {

		JdbcTemplate jdbcTemplate = new JdbcTemplate(conexion);
		String sql = "insert into partidas(id_balance,part_cod,pat_val) values(?,?,?)";

		for (final balanceDao balance : listPartidas) {
			final HashMap<String, BigDecimal> part = crearPartidasCargar(balance);
			for (final String key : part.keySet()) {
				final BigDecimal value = (BigDecimal) part.get(key);
				jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						ps.setInt(1, balance.getId_balance());
						ps.setString(2, key);
						ps.setBigDecimal(3, value);
					}

					public int getBatchSize() {
						return 1;
					}
				});

			}
		}

	}

	public static HashMap<String, BigDecimal> crearPartidasCargar(balanceDao balance) throws Exception {
		List<Method> metodoGet = getMetodo(balance);
		HashMap<String, BigDecimal> listaPartidas = new HashMap<String, BigDecimal>();
		for (Iterator iterator = metodoGet.iterator(); iterator.hasNext();) {
			Method method = (Method) iterator.next();
			Object valor = method.invoke(balance);
			if (valor != null && !method.getName().startsWith("getClass")
					&& !method.getName().startsWith("getId_balance") && !method.getName().startsWith("getNombre")) {
				BigDecimal valorBig = new BigDecimal(valor.toString());
				listaPartidas.put(method.getName().toUpperCase(), valorBig);
			}
		}
		return listaPartidas;
	}

	public static List<Method> getMetodo(balanceDao balance) {
		Method[] metodo = balance.getClass().getMethods();
		List<Method> listaMetodosSet = new ArrayList<Method>();
		for (Method method : metodo) {
			if (method.getName().startsWith("get")) {
				listaMetodosSet.add(method);
			}
		}
		return listaMetodosSet;
	}

}
