package com.zeiss.koch.kaffeekasse;

import android.text.Editable;

import java.util.Comparator;

/**
 * User
 * Created by koch on 01.10.2016.
 */

class User {

    private int id;
    private Role role;
    private String name;
    private String nfcid;

    public User(String name, String nfcid) {
        super();
        this.role = Role.user;
        this.name = name;
        this.nfcid = nfcid;
    }

    public User(int id, String name, String nfcid, String role) {
        super();
        this.role = Role.valueOf(role);
        this.id = id;
        this.name = name;
        this.nfcid = nfcid;
    }

    //getters & setters

    @Override
    public String toString() {
        return "User [id=" + id + ", role=" + role + ", name=" + name + ", nfcid=" + nfcid + "]";
    }

    String getName() {
        return name;
    }

    String getNfcId() {
        return nfcid;
    }

    public int getId() {
        return id;
    }

    public String getRole() {
        return ConvertRoleToDatabaseString(role);
    }

    public void setRole(Role role)
    {
        this.role = role;
    }

    public void SetNfcId(String nfcId) {
        this.nfcid = nfcId;
    }

    public boolean isAdmin() {
        if (this.role == Role.admin || this.role == Role.treasurer)
        {
            return true;
        }
        return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum Role
    {
        user,
        admin,
        treasurer
    }

    private static String ConvertRoleToDatabaseString(Role role)
    {
        switch(role)
        {
            case user:
                return "user";
            case admin:
                return "admin";
            case treasurer:
                return "treasurer";
            default:
                throw new IndexOutOfBoundsException("Role is unknown.");
        }
    }

    public static String ConvertRoleToGuiString(Role role)
    {
        switch(role)
        {
            case user:
                return "User";
            case admin:
                return "Admin";
            case treasurer:
                return "Treasurer";
            default:
                throw new IndexOutOfBoundsException("Role is unknown.");
        }
    }


}
