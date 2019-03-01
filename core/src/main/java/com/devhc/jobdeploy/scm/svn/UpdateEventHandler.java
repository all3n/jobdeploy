package com.devhc.jobdeploy.scm.svn;

import com.devhc.jobdeploy.tasks.ScmTask;
import com.devhc.jobdeploy.utils.Loggers;
import org.slf4j.Logger;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNStatusType;

public class UpdateEventHandler implements ISVNEventHandler {

  private Logger logger = Loggers.get(ScmTask.class);

  public void handleEvent(SVNEvent event, double progress) {
    /*
     * Gets the current action. An action is represented by SVNEventAction.
     * In case of an update an action can be determined via comparing
     * SVNEvent.getAction() and SVNEventAction.UPDATE_-like constants.
     */
    SVNEventAction action = event.getAction();
    String pathChangeType = " ";
    if (action == SVNEventAction.UPDATE_ADD) {
      /*
       * the item was added
       */
      pathChangeType = "A";
    } else if (action == SVNEventAction.UPDATE_DELETE) {
      /*
       * the item was deleted
       */
      pathChangeType = "D";
    } else if (action == SVNEventAction.UPDATE_UPDATE) {
      /*
       * Find out in details what state the item is (after having been
       * updated).
       * 
       * Gets the status of file/directory item contents. It is
       * SVNStatusType who contains information on the state of an item.
       */
      SVNStatusType contentsStatus = event.getContentsStatus();
      if (contentsStatus == SVNStatusType.CHANGED) {
        /*
         * the item was modified in the repository (got the changes from
         * the repository
         */
        pathChangeType = "U";
      } else if (contentsStatus == SVNStatusType.CONFLICTED) {
        /*
         * The file item is in a state of Conflict. That is, changes
         * received from the repository during an update, overlap with
         * local changes the user has in his working copy.
         */
        pathChangeType = "C";
      } else if (contentsStatus == SVNStatusType.MERGED) {
        /*
         * The file item was merGed (those changes that came from the
         * repository did not overlap local changes and were merged into
         * the file).
         */
        pathChangeType = "G";
      }
    } else if (action == SVNEventAction.UPDATE_EXTERNAL) {
      /* for externals definitions */
      logger.info("Fetching external item into '"
          + event.getFile().getAbsolutePath() + "'");
      logger.info("External at revision " + event.getRevision());
      return;
    } else if (action == SVNEventAction.UPDATE_COMPLETED) {
      /*
       * Working copy update is completed. Prints out the revision.
       */
      logger.info("At revision " + event.getRevision());
      return;
    } else if (action == SVNEventAction.ADD) {
      logger.info("A     " + event.getFile().getPath());
      return;
    } else if (action == SVNEventAction.DELETE) {
      logger.info("D     " + event.getFile().getPath());
      return;
    } else if (action == SVNEventAction.LOCKED) {
      logger.info("L     " + event.getFile().getPath());
      return;
    } else if (action == SVNEventAction.LOCK_FAILED) {
      logger.info("failed to lock    " + event.getFile().getPath());
      return;
    }

    /*
     * Status of properties of an item. SVNStatusType also contains
     * information on the properties state.
     */
    SVNStatusType propertiesStatus = event.getPropertiesStatus();
    String propertiesChangeType = " ";
    if (propertiesStatus == SVNStatusType.CHANGED) {
      /*
       * Properties were updated.
       */
      propertiesChangeType = "U";
    } else if (propertiesStatus == SVNStatusType.CONFLICTED) {
      /*
       * Properties are in conflict with the repository.
       */
      propertiesChangeType = "C";
    } else if (propertiesStatus == SVNStatusType.MERGED) {
      /*
       * Properties that came from the repository were merged with the
       * local ones.
       */
      propertiesChangeType = "G";
    }

    /*
     * Gets the status of the lock.
     */
    String lockLabel = " ";
    SVNStatusType lockType = event.getLockStatus();

    if (lockType == SVNStatusType.LOCK_UNLOCKED) {
      /*
       * The lock is broken by someone.
       */
      lockLabel = "B";
    }

    logger.info(pathChangeType + propertiesChangeType + lockLabel
        + "       " + event.getFile().getPath());
  }

  public void checkCancelled() throws SVNCancelException {
  }

}
