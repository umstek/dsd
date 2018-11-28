package lk.uom.cse14.dsd.ui;

import lk.uom.cse14.dsd.query.QueryTask;

public interface QueryTaskListener {
    void notifyQueryComplete(QueryTask queryTask);
}
