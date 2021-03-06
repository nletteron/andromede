package fr.andromede.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import fr.andromede.common.utils.Utils;
import fr.andromede.dao.SequencerBuilder;
import fr.andromede.dao.UserDAO;
import fr.andromede.dto.common.impl.UserDTO;

@Repository("userDAO")
public class UserDAOImpl extends UserDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserDAOImpl.class);

	@PostConstruct
	public void init() {
		setSequencer(SequencerBuilder.create().setDB(getTemplate().getCollection(UserDTO.class.getName()).getDB()).setCollection(UserDTO.class.getName()));
	}

	public UserDTO create(UserDTO userDTO) throws DataAccessException {
		LOGGER.trace("============================== DAO ================================");
		LOGGER.trace("START -- create(UserDTO)");
		LOGGER.trace(Utils.toString(userDTO));
		if ( userDTO != null ) {
			if ( userDTO.getId() == null || "".equalsIgnoreCase(userDTO.getId().trim()) ) {
				try {
					String nextID = getSequencer().getNextID();
					userDTO.setId(nextID);
					LOGGER.trace("Création d'un nouveau UserDTO avec pour ID : [{}].", nextID);
					LOGGER.debug(Utils.toString(userDTO));
					getTemplate().insert(userDTO);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					throw e;
				}
			} else {
				throw new InvalidDataAccessResourceUsageException("L'identifiant de l'utilisateur existe déjà en table. Création impossible.");
			}
		} else {
			throw new InvalidDataAccessResourceUsageException("La création d'un utilisateur [null] est impossible.");
		}
		LOGGER.trace("END   -- create(UserDTO )");
		LOGGER.trace("===================================================================");
		return userDTO;
	}

	public UserDTO read(String key) throws DataAccessException {
		UserDTO user = null;
		LOGGER.trace("============================== DAO ================================");
		LOGGER.trace("START -- read({})", key);
		if (key != null) {
			Query query = new Query(Criteria.where("id").is(key));
			user = (UserDTO)getTemplate().findOne(query, UserDTO.class);
		} else {
			throw new InvalidDataAccessResourceUsageException("La clef [null] ne peut être recherchée.");
		}
		LOGGER.debug(Utils.toString(user));
		LOGGER.trace("END   -- read({})", key);
		LOGGER.trace("===================================================================");
		return user;
	}

	public List<UserDTO> read(UserDTO userDTO) throws DataAccessException {
		LOGGER.trace("============================== DAO ================================");
		LOGGER.trace("START -- read(UserDTO)");
		List<UserDTO> users = new ArrayList<UserDTO>();
		if ( userDTO != null ) {
			LOGGER.trace(Utils.toString(userDTO));
			Map<String, Object> attributes = Utils.mapAttributes(userDTO);
			Criteria critere = null;
			boolean firstElement = true;
			for ( Entry<String, Object> entry : attributes.entrySet() ) {
				if ( !firstElement ) {
					critere = critere.and(entry.getKey()).is(entry.getValue());
				} else {
					critere = Criteria.where(entry.getKey()).is(entry.getValue());
					firstElement = false;
				}
			}
			Query query = new Query(critere);
			LOGGER.trace(Utils.toString(critere));
			users = getTemplate().find(query, UserDTO.class);
			LOGGER.debug(Utils.toString(users));
		} else {
			throw new InvalidDataAccessResourceUsageException("La recherche d'un utilisateur ne fonctionne pas sur un paramètre [null].");
		}
		LOGGER.trace("END   -- read(UserDTO)");
		LOGGER.trace("===================================================================");
		return users;
			}

	public UserDTO update(UserDTO userDTO) throws DataAccessException {
		LOGGER.trace("============================== DAO ================================");
		LOGGER.trace("START -- update(UserDTO)");
		LOGGER.debug(Utils.toString(userDTO));
		if ( userDTO != null ) {
			if ( userDTO.getId() != null && !"".equalsIgnoreCase(userDTO.getId().trim()) ) {
				Query query = new Query(Criteria.where("id").is(userDTO.getId()));
				Update update = new Update();
				Map<String, Object> map = Utils.mapAttributes(userDTO);
				for ( Entry<String, Object> entry : map.entrySet() ) {
					update.set((String)entry.getKey(), entry.getValue());
				}
				getTemplate().updateFirst(query, update, UserDTO.class);
			} else {
				throw new InvalidDataAccessResourceUsageException("Les mises à jour de l'utilisateur sont impossibles. Aucune clef unique transmise.");
			}
		} else {
			throw new InvalidDataAccessResourceUsageException("Les mises à jour de l'utilisateur [null] sont impossibles.");
		}
		LOGGER.trace("END   -- update(UserDTO)");
		LOGGER.trace("===================================================================");
		return userDTO;
	}

	public void deleteAll() throws DataAccessException {
		LOGGER.trace("============================== DAO ================================");
		LOGGER.trace("START -- deleteAll({})", getClass().getName());
		getTemplate().dropCollection(UserDTO.class);
		LOGGER.trace("===================================================================");
	}

	public void delete(String key) throws DataAccessException {
		LOGGER.trace("============================== DAO ================================");
		LOGGER.trace("START -- delete({})", key);
		if ( key != null ) {
			Query query = new Query(Criteria.where("id").is(key));
			getTemplate().remove(query, UserDTO.class);
		} else {
			throw new InvalidDataAccessResourceUsageException("La clef [null] ne peut être utilisée pour la suppression.");
		}
		LOGGER.trace("END   -- delete({})", key);
		LOGGER.trace("===================================================================");
	}

	public void delete(UserDTO userDTO) throws DataAccessException {
		LOGGER.trace("============================== DAO ================================");
		LOGGER.trace("START -- delete(UserDTO)");
		LOGGER.debug(Utils.toString(userDTO));
		getTemplate().remove(userDTO);
		LOGGER.trace("END   -- delete(UserDTO )");
		LOGGER.trace("===================================================================");
	}
}
