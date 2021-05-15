#!/usr/bin/env python
# coding: utf-8

# In[31]:


import joblib
import numpy as np


# In[32]:


clf1= joblib.load('modelo_entredado.pkl')


# In[68]:


x=np.array([[1,1,1,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0]])


# In[69]:


x


# In[70]:


prediccion = clf1.predict(x)
if prediccion == 1:
  print('Cambio de aceite')
if prediccion == 2:
  print('Mantenimiento general')
if prediccion == 3:
  print('Cambio de filtro de aire')
if prediccion == 4:
  print('Cambio de pastillas de freno')

