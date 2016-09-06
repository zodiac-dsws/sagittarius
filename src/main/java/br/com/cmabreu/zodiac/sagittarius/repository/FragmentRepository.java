package br.com.cmabreu.zodiac.sagittarius.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.entity.Activity;
import br.com.cmabreu.zodiac.sagittarius.entity.Fragment;
import br.com.cmabreu.zodiac.sagittarius.entity.Relation;
import br.com.cmabreu.zodiac.sagittarius.exceptions.DatabaseConnectException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.InsertException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.NotFoundException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.UpdateException;
import br.com.cmabreu.zodiac.sagittarius.infra.DaoFactory;
import br.com.cmabreu.zodiac.sagittarius.infra.IDao;

public class FragmentRepository extends BasicRepository {

	public FragmentRepository() throws DatabaseConnectException {
		super();
		debug("init");
	}
	
	public List<Fragment> getList( int idExperiment ) throws NotFoundException {
		debug("get fragment list" );
		DaoFactory<Fragment> df = new DaoFactory<Fragment>();
		IDao<Fragment> fm = df.getDao(this.session, Fragment.class);
		List<Fragment> fragments = null;
		try {
			fragments = fm.getList("select * from fragments where id_experiment = " + idExperiment);
		} catch (Exception e) {
			closeSession();
			throw e;
		}
		closeSession();
		debug("done: " + fragments.size() + " fragments.");
		return fragments;
	}

	
	public Fragment insertFragment(Fragment fragment) throws InsertException {
		debug("insert");
		DaoFactory<Fragment> df = new DaoFactory<Fragment>();
		IDao<Fragment> fm = df.getDao(this.session, Fragment.class);
		try {
			fm.insertDO(fragment);
			commit();
		} catch (Exception e) {
			error( e.getMessage() );
			rollBack();
			closeSession();
			throw new InsertException(e.getMessage());
		}
		closeSession();
		debug("done");
		return fragment;
	}

	
	public void insertFragmentList( List<Fragment> fragmentList ) throws InsertException {
		debug("insert");
		DaoFactory<Fragment> df = new DaoFactory<Fragment>();
		IDao<Fragment> fm = df.getDao(this.session, Fragment.class);
		try {
			for ( Fragment fragment : fragmentList ) {
				
				DaoFactory<Relation> dfr = new DaoFactory<Relation>();
				IDao<Relation> fmr = dfr.getDao(this.session, Relation.class);
				
				debug("fragment " + fragment.getSerial() + " : ");
				for ( Activity act : fragment.getActivities() ) {
					debug(" > activity " + act.getTag() );
					Set<Relation> inputRelations = new HashSet<Relation>();
					for ( Relation rel : act.getInputRelations() ) {
						debug("  > input " + rel.getName() );
						inputRelations.add( fmr.getDO( rel.getIdTable() ) );
					}
					act.setInputRelations(inputRelations);
				}
				fm.insertDO(fragment);
			}
			commit();
		} catch (Exception e) {
			error( e.getMessage() );
			rollBack();
			closeSession();
			throw new InsertException(e.getMessage());
		}
		closeSession();
		debug("done");
	}
	
	public void updateFragment( Fragment fragment ) throws UpdateException {
		debug("update");
		DaoFactory<Fragment> df = new DaoFactory<Fragment>();
		IDao<Fragment> fm = df.getDao(this.session, Fragment.class);
		try {
			fm.updateDO(fragment);
			commit();
		} catch (UpdateException e) {
			error( e.getMessage() );
			rollBack();
			closeSession();
			throw e;
		}
		closeSession();
		debug("done");
	}	
	
	public Fragment getFragment(int idFragment) throws NotFoundException {
		debug("retrieve");
		DaoFactory<Fragment> df = new DaoFactory<Fragment>();
		IDao<Fragment> fm = df.getDao(this.session, Fragment.class);
		Fragment fragment = null;
		try {
			fragment = fm.getDO(idFragment);
		} catch ( Exception e ) {
			closeSession();
			throw e;
		}
		closeSession();
		debug("done");
		return fragment;
	}

	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	

	private void error( String s ) {
		Logger.getInstance().error(this.getClass().getName(), s );
	}		
	
}
