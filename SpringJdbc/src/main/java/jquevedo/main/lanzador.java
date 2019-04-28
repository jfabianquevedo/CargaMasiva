package jquevedo.main;

import java.util.List;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.Soundbank;
import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import jquevedo.Dao.conexion;
import jquevedo.model.balanceDao;

public class lanzador {

	public static void main(String[] args) throws Exception {
		ApplicationContext contexto = new ClassPathXmlApplicationContext("Config.xml");
		conexion con = (conexion) contexto.getBean("conexion");
		ArrayList<balanceDao> listBalances = new ArrayList<balanceDao>();
//		HashMap<String, BigDecimal> listPartidas = new HashMap<String, BigDecimal>();

		for (int i = 0; i <1000; i++) {
			balanceDao balDao = new balanceDao();
			balDao.setNombre(i + "_balance");
//			balDao.setNombre(i+"12345");
			balDao.setAC("10");
			balDao.setACC("11");
			balDao.setP("20");
			balDao.setACL("4587");
			listBalances.add(balDao);
//			listPartidas = crearPartidasCargar(balDao);

		}

		try {
			long start = System.currentTimeMillis();
//			conexion.guardar(listBalances);
//			conexion.guardarBatchTemplate(listBalances);
			listBalances = conexion.guardarBatchSimpleInsert(listBalances);
			conexion.guardarPartidasPS(listBalances);

			long end = System.currentTimeMillis();
			System.out.println("tiempo Ejecucion: " + TimeUnit.MILLISECONDS.toSeconds(end - start));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
}
