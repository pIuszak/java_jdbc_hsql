package main.java.com.project.crud.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import main.java.com.project.crud.domain.Ammunition;
import main.java.com.project.crud.domain.Weapon;

public class AmmunitionManager implements IAmmunitionManager
{
	private Connection connection;

	private String url = "jdbc:hsqldb:hsql://localhost/workdb";

	private String createTableAmmunition = "CREATE TABLE Ammunition(id INT GENERATED BY DEFAULT AS IDENTITY, name varchar(20) unique, cost int, caliber int, Weapon_id int, FOREIGN KEY(Weapon_id) REFERENCES Weapon(id) ON DELETE CASCADE ON UPDATE CASCADE)";

	private PreparedStatement PS_add_one;
	private PreparedStatement PS_delete_all;
	private PreparedStatement PS_delete_one;
	private PreparedStatement PS_get_all;
	private PreparedStatement PS_update;
	private PreparedStatement PS_update_Weapon;
	private PreparedStatement PS_set_Weapon;
	private PreparedStatement PS_get_for_Weapon;

	private Statement statement;

	public AmmunitionManager()
	{
		try
		{
			connection = DriverManager.getConnection(url);
			statement = connection.createStatement();

			ResultSet rs = connection.getMetaData().getTables(null, null, null, null);
			boolean tableExists = false;
			while (rs.next())
			{
				if ("Ammunition".equalsIgnoreCase(rs.getString("TABLE_NAME")))
				{
					tableExists = true;
					break;
				}
			}

			if (!tableExists)
				statement.executeUpdate(createTableAmmunition);

			PS_add_one = connection
					.prepareStatement("INSERT INTO Ammunition (name, cost, caliber, Weapon_id) VALUES (?,?,?,?)");
			PS_delete_one = connection.prepareStatement("DELETE FROM Ammunition WHERE name=?");
			PS_delete_all = connection.prepareStatement("DELETE FROM Ammunition");
			PS_get_all = connection.prepareStatement("SELECT id, name, cost, caliber, Weapon_id FROM Ammunition");
			PS_update = connection
					.prepareStatement("UPDATE Ammunition SET name=?, cost=?, caliber=?, Weapon_id=? WHERE name=?");
			PS_update_Weapon = connection.prepareStatement("UPDATE Ammunition SET Weapon_id=? WHERE name=?");
			PS_set_Weapon = connection.prepareStatement(
					"UPDATE Ammunition SET Weapon_id=(SELECT id FROM Weapon WHERE name=?) WHERE name=?;");
			PS_get_for_Weapon = connection.prepareStatement(
					"SELECT id, name, cost, caliber, Weapon_id FROM Ammunition WHERE Weapon_id = (SELECT id FROM Weapon WHERE model=?);");
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public Connection getConnection()
	{
		return connection;
	}

	public void set_Weapon_for_Ammunition(Ammunition Ammunition, String Weapon_model)
	{
		try
		{
			PS_set_Weapon.setString(1, Weapon_model);
			PS_set_Weapon.setString(2, Ammunition.getName());
			System.out.println(PS_set_Weapon.executeUpdate());
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public boolean add_Ammunition(Ammunition Ammunition)
	{
		int count = 0;
		try
		{
			PS_add_one.setString(1, Ammunition.getName());
			PS_add_one.setInt(2, Ammunition.getCost());
			PS_add_one.setInt(3, Ammunition.getcaliber());
			PS_add_one.setInt(4, Ammunition.getWeapon_id());

			count = PS_add_one.executeUpdate();

		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		if (count == 1)
		{
			return true;
		} else
		{
			return false;
		}
	}

	public boolean add_all_Ammunitions(List<Ammunition> Ammunitions)
	{
		try
		{
			connection.setAutoCommit(false);

			for (Ammunition Ammunition : Ammunitions)
			{
				PS_add_one.setString(1, Ammunition.getName());
				PS_add_one.setInt(2, Ammunition.getCost());
				PS_add_one.setInt(3, Ammunition.getcaliber());
				PS_add_one.setInt(4, Ammunition.getWeapon_id());
				PS_add_one.executeUpdate();
			}

			connection.commit();
			connection.setAutoCommit(true);
		} catch (SQLException e)
		{
			try
			{

				connection.rollback();
				connection.setAutoCommit(true);
			} catch (SQLException e1)
			{
				e1.printStackTrace();
			}
		}

		return false;
	}

	public int delete_Ammunition(Ammunition Ammunition)
	{
		int count = 0;
		try
		{
			PS_delete_one.setString(1, Ammunition.getName());
			count = PS_delete_one.executeUpdate();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return count;
	}

	public boolean update_Ammunition(Ammunition old, Ammunition new_ammo)
	{
		int count = 0;
		try
		{
			// NEW
			PS_update.setString(1, new_ammo.getName());
			PS_update.setInt(2, new_ammo.getCost());
			PS_update.setInt(3, new_ammo.getcaliber());
			PS_update.setInt(4, new_ammo.getWeapon_id());
			// OLD
			PS_update.setString(5, old.getName());

			count = PS_update.executeUpdate();

		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		if (count == 1)
			return true;
		else
			return false;
	}

	// DELETE
	public boolean clear_Ammunition(Ammunition Ammunition)
	{
		int count = 0;
		try
		{
			PS_delete_one.setString(1, Ammunition.getName());
			count = PS_delete_one.executeUpdate();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		if (count == 1)
			return true;
		else
			return false;

	}

	public void delete_all_Ammunitions()
	{
		try
		{
			PS_delete_all.executeUpdate();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}

	}

	// SELECT
	public List<Ammunition> get_all_Ammunitions()
	{
		List<Ammunition> ammunitions = new ArrayList<Ammunition>();

		try
		{
			ResultSet rs = PS_get_all.executeQuery();

			while (rs.next())
			{
				Ammunition p = new Ammunition();
				p.setId(rs.getInt("id"));
				p.setName(rs.getString("name"));
				p.setCost(rs.getInt("cost"));
				p.setcaliber(rs.getInt("caliber"));
				p.setWeapon_id(rs.getInt("Weapon_id"));
				ammunitions.add(p);
			}

		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return ammunitions;
	}

	public List<Ammunition> get_all_Ammunitions_for_Weapons(String model)
	{
		List<Ammunition> ammo_list = new ArrayList<Ammunition>();

		try
		{
			PS_get_for_Weapon.setString(1, model);
			ResultSet rs = PS_get_for_Weapon.executeQuery();

			while (rs.next())
			{
				Ammunition m = new Ammunition();
				m.setName(rs.getString("name"));
				m.setCost(rs.getInt("cost"));
				m.setcaliber(rs.getInt("caliber"));
				m.setWeapon_id(rs.getInt("Weapon_id"));
				ammo_list.add(m);
			}

		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return ammo_list;
	}



}
