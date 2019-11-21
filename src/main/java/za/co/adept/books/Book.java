/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.adept.books;

/**
 *
 * @author marlon
 */
class Book {
    
    private Long isbn;
    private String title;
    
    public Book() {
        // default constructor
    }

    public Book(Long isbn, String name) {
        this.isbn = isbn;
        this.title = name;
    }

    public Long getIsbn() {
        return isbn;
    }

    public void setIsbn(Long isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
}
