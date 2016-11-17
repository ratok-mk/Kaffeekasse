package com.zeiss.koch.kaffeekasse;

/**
 * User
 * Created by koch on 01.10.2016.
 */

class User {

    public enum Role
    {
        USER,
        ADMIN
    }

    private int id;
    private String name;
    private String nfcid;
    private Role role;

    private static final int UNPERSISTED = -1;

    public User(String name, String nfcid) {
        super();
        this.id = UNPERSISTED;
        this.name = name;
        this.nfcid = nfcid;
        this.role = Role.USER;
    }

    public User(int id, String name, String nfcid, String role) {
        super();
        this.id = id;
        this.name = name;
        this.nfcid = nfcid;
        this.role = ConvertDatabaseStringToRole(role);
    }

    //getters & setters

    @Override
    public String toString() {
        return "User [id=" + id + ", role=" + role + ", name=" + name + ", nfcid=" + nfcid + "]";
    }

    public int getId() {
        return id;
    }

    String getName() {
        return name;
    }

    String getNfcId() {
        return nfcid;
    }

    public String getRole() {
        return ConvertRoleToDatabaseString(role);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNfcId(String nfcId) {
        this.nfcid = nfcId;
    }

    public void setRole(Role role)
    {
        this.role = role;
    }

    public boolean isAdmin() {
        return (this.role == Role.ADMIN);
    }

    public boolean isPersisted() {
        return this.id != UNPERSISTED;
    }

    private static String ConvertRoleToDatabaseString(Role role)
    {
        switch(role)
        {
            case USER:
                return "user";
            case ADMIN:
                return "admin";
            default:
                throw new IndexOutOfBoundsException("Role is unknown.");
        }
    }

    private static Role ConvertDatabaseStringToRole(String dbString)
    {
        switch(dbString)
        {
            case "user":
                return Role.USER;
            case "admin":
                return Role.ADMIN;
            default:
                throw new IndexOutOfBoundsException("Database role is unknown.");
        }
    }

    public static String ConvertRoleToGuiString(Role role)
    {
        switch(role)
        {
            case USER:
                return "User";
            case ADMIN:
                return "Admin";
            default:
                throw new IndexOutOfBoundsException("Role is unknown.");
        }
    }
}
