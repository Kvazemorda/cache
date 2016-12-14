package com.dao;

import com.entity.Word;
import com.hibernate.HibernateSessionFactory;
import org.hibernate.Query;
import org.hibernate.Session;

public class WordDAO {

    public Word getWord(String word){
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        String hql = "select word from Word word " +
                "where word.word = :wordQuery";

        Query query = session.createQuery(hql);
        query.setParameter("wordQuery", word);
        Word wordResponse = (Word) query.list().get(0);

        return wordResponse;
    }
}
