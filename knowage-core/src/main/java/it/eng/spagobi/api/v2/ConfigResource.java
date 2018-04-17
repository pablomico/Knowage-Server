/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2;

import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/configs")
@ManageAuthorization
public class ConfigResource extends AbstractSpagoBIResource {

	private static Logger logger = Logger.getLogger(ConfigResource.class);

	@GET
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public List<Config> getConfigs() {
		logger.debug("IN");
		try {
			IConfigDAO configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());
			return configsDao.loadAllConfigParameters();

		} catch (Exception e) {
			logger.error("Error while getting the list of configs", e);
			throw new SpagoBIRuntimeException("Error while getting the list of configs", e);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Config getSingleConfig(@PathParam("id") Integer id) {
		logger.debug("IN");
		try {
			IConfigDAO configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());
			Config config = configsDao.loadConfigParametersById(id);
			if (config == null) {
				logger.error("Config with id " + id + " not present in current tenant");
			}
			return config;
		} catch (Exception e) {
			logger.error("Error while getting config " + id, e);
			throw new SpagoBIRuntimeException("Error while getting config " + id, e);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/label/{label}")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Config getSingleConfigByLabel(@PathParam("label") String label) {
		logger.debug("IN");
		try {
			IConfigDAO configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());
			Config config = configsDao.loadConfigParametersByLabel(label);
			if (config == null) {
				logger.error("Config with label " + label + " not present in current tenant");
			}
			return config;
		} catch (Exception e) {
			logger.error("Error while getting config " + label, e);
			throw new SpagoBIRuntimeException("Error while getting config " + label, e);
		} finally {
			logger.debug("OUT");
		}
	}

	/*
	 * added as separated service because it is public
	 */
	@GET
	@Path("/label/KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Config getKnowageCalculatedFunctionConfig() {
		logger.debug("IN");
		IConfigDAO configsDao = null;
		// List<Config> allObjects = null;
		Config dm = null;
		try {
			configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());
			dm = configsDao.loadConfigParametersByLabel("KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS");
			if (dm == null) {
				logger.error("Config with label KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS not present in current tenant");
				return null;
			} else if (dm.getLabel().equals("KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS")) {
				return dm;
			}
		} catch (Exception e) {
			logger.error("Error while getting config KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS", e);
			throw new SpagoBIRuntimeException("Error while getting config KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS", e);
		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	@Consumes("application/json")
	public Response insertConfig(@Valid Config body) {

		IConfigDAO configsDao = null;
		Config config = body;
		if (config == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (config.getId() != null) {
			return Response.status(Status.BAD_REQUEST).entity("Error paramters. New config should not have ID value").build();
		}

		try {
			configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());
			configsDao.saveConfig(config);
			String encodedConfig = URLEncoder.encode("" + config.getId(), "UTF-8");
			return Response.created(new URI("1.0/configs/" + encodedConfig)).entity(encodedConfig).build();
		} catch (Exception e) {
			Response.notModified().build();
			logger.error("Error while creating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while creating url of the new resource", e);
		}
	}

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateConfig(@PathParam("id") Integer id, @Valid Config body) {

		IConfigDAO configsDao = null;
		Config config = body;

		if (config == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (config.getId() == null) {
			return Response.status(Status.NOT_FOUND).entity("The config with ID " + id + " doesn't exist").build();
		}

		try {
			configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());
			configsDao.saveConfig(config);
			String encodedConfig = URLEncoder.encode("" + config.getId(), "UTF-8");
			return Response.created(new URI("1.0/configs/" + encodedConfig)).entity(encodedConfig).build();
		} catch (Exception e) {
			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while updating url of the new resource", e);
		}
	}

	@PUT
	@Path("/conf")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateConfig(String body) { // JSON obj

		JSONObject jsonobject = null;
		JSONArray jsonArray = null;
		URI uri = null;

		try {
			jsonobject = new JSONObject(body);
			jsonArray = jsonobject.getJSONArray("configurations");

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject configObject = jsonArray.getJSONObject(i);
				saveReceivedConfig(configObject);
			}

			uri = new URI("2.0/conf");
			return Response.created(uri).build();

		} catch (Exception e) {
			logger.error("Error updating config", e);
			return Response.notModified(e.getMessage()).build();
		}

	}

	private void saveReceivedConfig(JSONObject configObject) {
		Config c = null;
		IConfigDAO configsDao = null;
		String label = null, value = null;
		try {
			label = configObject.getString("label");
			value = configObject.getString("value");
			configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());
			c = configsDao.loadConfigParametersByLabel(label);
			c.setValueCheck(value);
			configsDao.saveConfig(c);
		} catch (Exception e) {
			logger.error("Error while saving received configuration", e);
			throw new SpagoBIRuntimeException("Error while saving received configuration", e);
		}

	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.CONFIG_MANAGEMENT })
	public Response deleteConfig(@PathParam("id") Integer id) {
		try {
			IConfigDAO configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());
			configsDao.delete(id);
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);
		}
	}
}
