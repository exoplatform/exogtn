/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.web.security.security;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.chromattic.ContextualTask;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.web.security.GateInToken;
import org.exoplatform.web.security.codec.AbstractCodec;
import org.exoplatform.web.security.codec.AbstractCodecBuilder;
import org.exoplatform.web.security.codec.ToThrowAwayCodec;
import org.gatein.common.io.IOTools;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.security.Credentials;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Created by The eXo Platform SAS Author : liem.nguyen ncliam@gmail.com Jun 5,
 * 2009
 */
public class CookieTokenService extends AbstractTokenService<GateInToken, String>
{

   private static final Logger LOG = LoggerFactory.getLogger(CookieTokenService.class);

   /** . */
   public static final String LIFECYCLE_NAME="lifecycle-name";
	
   /** . */
   private ChromatticLifeCycle chromatticLifeCycle;
   
   /** . */
   private String lifecycleName="autologin";

   private AbstractCodec codec;

   public CookieTokenService(InitParams initParams, ChromatticManager chromatticManager)
   {
      super(initParams);

      if (initParams.getValuesParam(SERVICE_CONFIG).getValues().size() > 3)
      {
    	  lifecycleName = (String)initParams.getValuesParam(SERVICE_CONFIG).getValues().get(3);
      }
      this.chromatticLifeCycle = chromatticManager.getLifeCycle(lifecycleName);

      initCodec();
   }

   private void initCodec()
   {
      String builderType = PropertyManager.getProperty("gatein.codec.builderclass");
      Map<String, String> config = new HashMap<String, String>();

      if (builderType != null)
      {
         //If there is config for codec in configuration.properties, we read the config parameters from config file referenced in configuration.properties
         String configFile = PropertyManager.getProperty("gatein.codec.config");
         InputStream in = null;
         try
         {
            File f = new File(configFile);
            in = new FileInputStream(f);
            Properties properties = new Properties();
            properties.load(in);
            for (Map.Entry entry : properties.entrySet())
            {
               config.put(entry.getKey().toString(), entry.getValue().toString());
            }
            config.put("gatein.codec.config.basedir", f.getParentFile().getAbsolutePath());
         }
         catch (IOException ioEx)
         {
            LOG.warn("Failed to read the config parameters from " + configFile, ioEx);
         }
         finally
         {
            IOTools.safeClose(in);
         }
      }
      else
      {
         //If there is no config for codec in configuration.properties, we generate key if it does not exist and setup the default config
         builderType = "org.exoplatform.web.security.codec.JCASymmetricCodecBuilder";
         String gtnConfDir = PropertyManager.getProperty("gatein.conf.dir");
         File f = new File(gtnConfDir + "/codec/codeckey.txt");
         if (!f.exists())
         {
            new File(gtnConfDir + "/codec").mkdir();
            OutputStream out = null;
            try
            {
               KeyGenerator keyGen = KeyGenerator.getInstance("AES");
               keyGen.init(128);
               SecretKey key = keyGen.generateKey();
               KeyStore store = KeyStore.getInstance("JCEKS");
               store.load(null, "gtnStorePass".toCharArray());
               store.setEntry("gtnKey", new KeyStore.SecretKeyEntry(key), new KeyStore.PasswordProtection("gtnKeyPass".toCharArray()));
               f.createNewFile();
               out = new FileOutputStream(f);
               store.store(out, "gtnStorePass".toCharArray());
            }
            catch (Exception ex)
            {
               ex.printStackTrace();
            }
            finally
            {
               IOTools.safeClose(out);
            }
         }
         config.put("gatein.codec.jca.symmetric.keyalg", "AES");
         config.put("gatein.codec.jca.symmetric.keystore", "codeckey.txt");
         config.put("gatein.codec.jca.symmetric.storetype", "JCEKS");
         config.put("gatein.codec.jca.symmetric.alias", "gtnKey");
         config.put("gatein.codec.jca.symmetric.keypass", "gtnKeyPass");
         config.put("gatein.codec.jca.symmetric.storepass", "gtnStorePass");
         config.put("gatein.codec.config.basedir", f.getParentFile().getAbsolutePath());
      }

      try
      {
         this.codec = Class.forName(builderType).asSubclass(AbstractCodecBuilder.class).newInstance().build(config);
         LOG.info("Initialize successfully the codec with builder " + builderType);
      }
      catch (Exception ex)
      {
         LOG.warn("Failed to initialize the codec with builder " + builderType + " , use ToThrowAwayCodec", ex);
         this.codec = new ToThrowAwayCodec();
      }
   }

   public String createToken(final Credentials credentials)
   {
      if (validityMillis < 0)
      {
         throw new IllegalArgumentException();
      }
      if (credentials == null)
      {
         throw new NullPointerException();
      }
      return new TokenTask<String>() {
         @Override
         protected String execute()
         {
            String tokenId = nextTokenId();
            long expirationTimeMillis = System.currentTimeMillis() + validityMillis;
            GateInToken token = new GateInToken(expirationTimeMillis, credentials);
            TokenContainer container = getTokenContainer();

            //Save the token, password is encoded thanks to the codec
            container.encodeAndSaveToken(tokenId, token.getPayload(), new Date(expirationTimeMillis), codec);
            return tokenId;
         }
      }.executeWith(chromatticLifeCycle);
   }

   @Override
   public GateInToken getToken(final String id)
   {
      return new TokenTask<GateInToken>() {
         @Override
         protected GateInToken execute()
         {
            //Get the token, encoded password is decoded thanks to codec
            return getTokenContainer().getTokenAndDecode(id, codec);
         }
      }.executeWith(chromatticLifeCycle);
   }

   @Override
   public GateInToken deleteToken(final String id)
   {
      return new TokenTask<GateInToken>() {
         @Override
         protected GateInToken execute()
         {
            return getTokenContainer().removeToken((String)id);
         }
      }.executeWith(chromatticLifeCycle);
   }

   @Override
   public String[] getAllTokens()
   {
      return new TokenTask<String[]>() {
         @Override
         protected String[] execute()
         {
            TokenContainer container = getTokenContainer();
            Collection<TokenEntry> tokens = container.getAllTokens();
            String[] ids = new String[tokens.size()];
            int count = 0;
            for (TokenEntry token : tokens)
            {
               ids[count++] = token.getId();
            }
            return ids;
         }
      }.executeWith(chromatticLifeCycle);
   }

   @Override
   public long size() throws Exception
   {
      return new TokenTask<Long>() {
         @Override
         protected Long execute()
         {
            TokenContainer container = getTokenContainer();
            Collection<TokenEntry> tokens = container.getAllTokens();
            return (long)tokens.size();
         }
      }.executeWith(chromatticLifeCycle);
   }

   @Override
   protected String decodeKey(String stringKey)
   {
      return stringKey;
   }

   /**
    * Wraps token store logic conveniently.
    *
    * @param <V> the return type
    */
   private abstract class TokenTask<V> extends ContextualTask<V>
   {

      protected final TokenContainer getTokenContainer() {
         SessionContext ctx = chromatticLifeCycle.getContext();
         ChromatticSession session = ctx.getSession();
         TokenContainer container = session.findByPath(TokenContainer.class, lifecycleName);
         if (container == null)
         {
            container = session.insert(TokenContainer.class, lifecycleName);
         }
         return container;
      }

      @Override
      protected V execute(SessionContext context)
      {
         return execute();
      }

      protected abstract V execute();

   }
}
