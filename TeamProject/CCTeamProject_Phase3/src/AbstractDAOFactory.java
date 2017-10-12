
/**
 * Favorites Management.
 * 08-672 Homework #4
 * @author Han Na (hna1@andrew.cmu.edu)
 * December 12, 2015
 */
public abstract class AbstractDAOFactory {
    public abstract TweetDAO getTweetDAO();
    public static AbstractDAOFactory getDAOFactory() {
        return new DAOFactory();
    }
}
