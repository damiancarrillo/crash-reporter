/*

 Copyright (c) 2013, CDev LLC
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the <organization> nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package co.cdev.crashReporter.repository;

import co.cdev.crashReporter.model.Entity;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import java.util.List;

public abstract class AbstractRepository<E extends Entity> implements Repository<E> {

    private final Class<E> entityClass;

    public AbstractRepository(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public abstract E createEntity();

    @Override
    public long count(PersistenceManager pm) throws RepositoryException {
        long count = 0;

        try {
            Query query = pm.newQuery(getEntityClass());
            query.setResult("count(this)");
            query.setUnique(true);
            count = (Long) query.execute();
        } catch (JDOException ex) {
            throw new RepositoryException(ex);
        }

        return count;
    }

    @Override
    public List<E> fetch(PersistenceManager pm, long index, long count) throws RepositoryException {
        List<E> entities = null;

        try {
            Query query = pm.newQuery(getEntityClass());
            query.setOrdering("createdDate descending");
            query.setRange(index, index + count);

            entities = (List<E>) query.execute();
        } catch (JDOException ex) {
            throw new RepositoryException(ex);
        }

        return entities;
    }

}
