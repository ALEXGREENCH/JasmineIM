package ru.ivansuper.jasmin.icq;

/**
 * Represents the criteria used for searching users.
 * This class holds various fields that can be populated to refine a user search.
 */
public class SearchCriteries {
    public String nick = "";
    public String name = "";
    public String lastname = "";
    public int gender = 0;
    /** @noinspection unused*/
    public int age = 0;
    public String city = "";
    public int page = 0;
}