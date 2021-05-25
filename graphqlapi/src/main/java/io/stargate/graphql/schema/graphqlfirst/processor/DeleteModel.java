/*
 * Copyright The Stargate Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.stargate.graphql.schema.graphqlfirst.processor;

import graphql.language.FieldDefinition;
import graphql.schema.DataFetcher;
import io.stargate.graphql.schema.graphqlfirst.fetchers.deployed.DeleteFetcher;
import java.util.List;
import java.util.Optional;

public class DeleteModel extends MutationModel {

  private final EntityModel entity;
  private final Optional<String> entityArgumentName;
  private final List<ConditionModel> whereConditions;
  private final List<ConditionModel> ifConditions;
  private final ReturnType returnType;
  private final boolean ifExists;

  DeleteModel(
      String parentTypeName,
      FieldDefinition field,
      EntityModel entity,
      Optional<String> entityArgumentName,
      List<ConditionModel> whereConditions,
      List<ConditionModel> ifConditions,
      ReturnType returnType,
      boolean ifExists) {
    super(parentTypeName, field);
    this.entity = entity;
    this.entityArgumentName = entityArgumentName;
    this.whereConditions = whereConditions;
    this.ifConditions = ifConditions;
    this.returnType = returnType;
    this.ifExists = ifExists;
  }

  public EntityModel getEntity() {
    return entity;
  }

  /**
   * If the mutation takes a unique entity input argument, the name of that argument. Either this or
   * {@link #getWhereConditions()} is set.
   */
  public Optional<String> getEntityArgumentName() {
    return entityArgumentName;
  }

  /**
   * If the mutation takes individual PK fields, the condition builder associated with each field.
   * Either this or {@link #getEntityArgumentName()} is set.
   */
  public List<ConditionModel> getWhereConditions() {
    return whereConditions;
  }

  public List<ConditionModel> getIfConditions() {
    return ifConditions;
  }

  public ReturnType getReturnType() {
    return returnType;
  }

  public boolean ifExists() {
    return ifExists;
  }

  @Override
  public DataFetcher<?> getDataFetcher(MappingModel mappingModel) {
    return new DeleteFetcher(this, mappingModel);
  }
}
