package ru.ivansuper.jasmin.icq;

/**
 * Represents a single item in an ICQ search result.
 * This class holds information about a user found during a search,
 * such as their UIN, nickname, name, age, gender, status, and
 * whether authorization is required to add them as a contact.
 * It also indicates if this is the last item in the search results
 * and how many pages of results are available.
 */
public class SearchResultItem {
    public boolean isLast;
    public boolean need_auth;
    public String uin = "";
    public String nick = "";
    public String firstname = "";
    public String lastname = "";
    public int age = 0;
    public int gender = 0;
    public int status = 0;
    public int found_in_database = 0;
    public int pages_available = 0;
}