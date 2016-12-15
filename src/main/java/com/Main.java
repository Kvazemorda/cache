package com;

import com.dao.WordDAO;
import com.entity.Word;
import com.hibernate.HibernateSessionFactory;
import org.hibernate.Session;

import java.io.File;
import java.util.Date;
import java.util.Random;

/**
 * Поиск популярной информации к примеру слова в словаре.
 * В мире новости освещаяют различные события, читая новости, пользователь находит незнакомое слово
 * и ищет его в словаре. Из всего словаря популярностью пользуются в основном те слова которые мелькают
 * в новостях.
 * Пользователь находит слово в базе и оно падает в первый список in. Т.к. через пару мгновений пользователь
 * забывает описание слова, он повторяет просит еще раз показать слово и оно уже возвращается из списка in.
 * По мере поиска новых слов они перетикают из памяти в файловую систему и если пользователь запросил слово
 * из файловой системы(out), то это слово переходит в список main.
 * Вообщем изходя из закона Паррето: 20% популярных слов храним в main, 10% храним в in (то, что посмотрели
 * мунуту назад наверняка еще вернутся) и остальные 70% храним в out (в отстойнике).
 *
 */
public class Main {
    public static void main(String[] args) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();

        for (int i = 1; i <= 500; i++){
            Word word = new Word();
            word.setWord("Name " + i);
            word.setDescription("Description name " + i);
            session.getTransaction().begin();
            session.saveOrUpdate(word);
            session.getTransaction().commit();
        }
        session.close();

        Cache<Integer, Word> cache = new Cache<>(100);
        int countDB = 0;
        int size = 2500;
        Date timeStart1 = new Date();
        for(int i = 0; i < size; i++){
            int random = new Random().nextInt(200) + 1;
            String query = "Name " + random;
            Word response = cache.getV(query.hashCode());

            if(response == null){
                response = new WordDAO().getWord("Name " + random);
                cache.putValueFormDB(response.hashCode(),response);
                countDB++;
            }
        }
        Date timeEnd1 = new Date();

        Date timeStart2 = new Date();
        for(int i = 0; i < size; i++){
            int random = new Random().nextInt(50) + 1;
            String query = "Name " + random;
            Word response = new WordDAO().getWord("Name " + random);
        }
        Date timeEnd2 = new Date();
        System.out.println("time cache&DB " + (timeEnd1.getTime() - timeStart1.getTime()) + " vs time DB " + (timeEnd2.getTime() - timeStart2.getTime()));
        System.out.println(("db = " + (double)countDB / size ));
        System.out.println("in = " + (double)cache.countIn / size + " fill size: " + cache.sizeIn + " max size: " + cache.maxSizeIn);
        System.out.println("out = " + (double)cache.countOut / size + " fill size: " + cache.sizeOut + " max size: " + cache.maxSizeOut);
        System.out.println("main = " + (double)cache.countMain / size + " fill size: " + cache.sizeMain + " max size: " + cache.maxSizeMain);

        HibernateSessionFactory.shutdown();
        File file = new File(cache.getPathToCacheFiles());
        delete(file);

    }
    private static void delete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                c.delete();
        }
    }
}
