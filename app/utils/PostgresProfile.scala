package utils

package util

import com.github.tminglei.slickpg._
import org.locationtech.jts.geom.Geometry
import play.api.libs.json._
import slick.ast.Library._
import slick.ast._
import slick.basic.Capability
import slick.jdbc._
import slick.lifted.FunctionSymbolExtensionMethods._
import slick.lifted.Shape
import slick.relational.{CompiledMapping, ResultConverter}
import slick.sql.{FixedSqlAction, SqlAction, SqlStreamingAction}
import slick.util.SQLBuilder

import java.sql.JDBCType
import java.time.{Instant, LocalDateTime}
import java.util.UUID
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scala.util.chaining._

trait PostgresProfile extends ExPostgresProfile with PgDate2Support with PgPlayJsonSupport with PgArraySupport with PgPostGISSupport with str.PgStringSupport {

  override def pgjson: String = "jsonb"

  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api: PostgresAPI.type = PostgresAPI

  object PostgresAPI
      extends API
      with DateTimeImplicits
      with Date2DateTimePlainImplicits
      with PlayJsonImplicits
      with PgStringImplicits
      with PlayJsonPlainImplicits
      with ArrayImplicits
      with SimpleArrayPlainImplicits
      with PostGISImplicits
      with PostGISAssistants {

    val DefaultSRID = 4269

    val datePart: (Rep[String], Rep[LocalDateTime]) => Rep[Int] = SimpleFunction.binary[String, LocalDateTime, Int]("date_part")

    implicit class EnhancedGeometryColumn(geometry: Rep[Option[Geometry]]) {
      def union: Rep[Option[Geometry]] = SimpleFunction[Option[Geometry]]("ST_Union").apply(Seq(geometry))

      def makeValid: Rep[Option[Geometry]] = SimpleFunction[Option[Geometry]]("ST_MakeValid").apply(Seq(geometry))

      def union(g2: Rep[Option[Geometry]]): Rep[Option[Geometry]] = SimpleFunction[Option[Geometry]]("ST_Union").apply(Seq(geometry, g2))
    }



    implicit val simpleStrSeqTypeMapper: SimpleArrayJdbcType[String] = new SimpleArrayJdbcType[String]("text")
    implicit val localDateTimeWitness: ElemWitness[LocalDateTime] = ElemWitness.AnyWitness.asInstanceOf[ElemWitness[LocalDateTime]]
    implicit val simpleLocalDateTimeSeqTypeMapper: DriverJdbcType[List[LocalDateTime]] =
      new SimpleArrayJdbcType[LocalDateTime]("timestamp").to(_.toList)

    implicit val setStringSeqParameter: SetParameter[Seq[String]] = SetParameter[Seq[String]]((value, pp) => {
      simpleStrSeqTypeMapper.setValue(value, pp.ps, pp.tap(_.pos += 1).pos)
    })

    // aggregation functions in slick-pg don't appear to be compatible with groupBy
    // https://github.com/tminglei/slick-pg/issues/289
    val ArrayAgg = new SqlAggregateFunction("array_agg")
    val BoolAnd = new SqlAggregateFunction("bool_and")
    val BoolOr = new SqlAggregateFunction("bool_or")

    implicit class ArrayAggColumnQueryExtensionMethods[P, C[_]](val q: Query[Rep[P], _, C]) {
      def arrayAgg[B](implicit tm: TypedType[List[B]]): Rep[List[B]] = ArrayAgg.column[List[B]](q.toNode)

      def boolAnd(implicit tm: TypedType[Option[Boolean]]): Rep[Option[Boolean]] = BoolAnd.column[Option[Boolean]](q.toNode)

      def boolOr(implicit tm: TypedType[Option[Boolean]]): Rep[Option[Boolean]] = BoolOr.column[Option[Boolean]](q.toNode)
    }

    val nowInstant = SimpleLiteral[Instant]("now()")

    implicit val getResultUUID: GetResult[UUID] = GetResult[UUID](pr => UUID.fromString(pr.nextString()))

    implicit object SetUUID extends SetParameter[UUID] {
      override def apply(uuid: UUID, pp: PositionedParameters): Unit =
        pp.setObject(uuid, JDBCType.BINARY.getVendorTypeNumber)
    }

    implicit val SetOptUUID: SetParameter[Option[UUID]] = SetParameter[Option[UUID]] {
      case (Some(v), pp) => SetUUID(v, pp)
      case (_, pp) => pp.setNull(JDBCType.OTHER.getVendorTypeNumber())
    }

    implicit val playJsonObjectTypeMapper: JdbcType[JsObject] = new GenericJdbcType[JsObject](
      pgjson,
      Json.parse(_).as[JsObject],
      Json.stringify(_).replace("\\u0000", ""),
      hasLiteralForm = false
    )

    implicit def jsObjectTypeMapper[T](implicit format: OFormat[T], classTag: ClassTag[T]): BaseColumnType[T] =
      MappedColumnType.base[T, JsObject](Json.toJsObject(_), _.as[T])

    implicit def jsObjectMapTypeMapper[T](implicit format: Format[T]): BaseColumnType[Map[String, T]] =
      MappedColumnType.base[Map[String, T], JsObject](Json.toJsObject(_), _.as[Map[String, T]](Reads.mapReads(format)))

    implicit def jsArrayTypeMapper[T <: Product](implicit format: Format[T]): BaseColumnType[Seq[T]] =
      MappedColumnType.base[Seq[T], JsValue](values => JsArray(values.map(value => Json.toJson(value)(format))), _.as[Seq[T]])

    implicit class EnhancedIntSqlAction[-E <: Effect](action: SqlAction[Int, NoStream, E]) {
      def atLeastOneIsSome(implicit ec: ExecutionContext): DBIOAction[Option[Unit], NoStream, E] =
        action.map(x => if (x > 0) Some(()) else None)
    }

    // https://www.missingfaktor.me/writing/2018/08/12/composable-table-updates-in-slick/
    sealed trait Parameter[RecordA] {
      self =>
      type Field
      type Value

      def field: RecordA => Field

      def newValue: Value

      def shape: slick.lifted.Shape[_ <: FlatShapeLevel, Field, Value, Field]

      final def apply[U, C[_]](query: Query[RecordA, U, C]): FixedSqlAction[Int, NoStream, Effect.Write] = {
        query.map(field)(shape).update(newValue)
      }

      final def updateReturning[U, C[_], A, F](query: Query[RecordA, U, C], returningQuery: Query[A, F, C]): SqlStreamingAction[Vector[F], F, Effect] = {
        query.map(field)(shape).updateReturning(returningQuery, newValue)
      }

      final def insert[U, C[_]](query: Query[RecordA, U, C]): FixedSqlAction[U, NoStream, Effect.Write] =
        (query.map(field)(shape) returning query) += newValue

      final def update[U, C[_]](query: Query[RecordA, U, C]): FixedSqlAction[Int, NoStream, Effect.Write] =
        query.map(field)(shape).update(newValue)

      final def and(another: Parameter[RecordA]): Parameter[RecordA] = new Parameter[RecordA] {
        type Field = (self.Field, another.Field)
        type Value = (self.Value, another.Value)

        def field: RecordA => Field = record => (self.field(record), another.field(record))

        def newValue: Value = (self.newValue, another.newValue)

        def shape: slick.lifted.Shape[_ <: FlatShapeLevel, Field, Value, Field] =
          Shape.tuple2Shape(self.shape, another.shape)
      }
    }

    object Parameter {
      def apply[RecordB, _Field, _Value](_field: RecordB => _Field, _newValue: _Value)(implicit
          _shape: slick.lifted.Shape[_ <: FlatShapeLevel, _Field, _Value, _Field]
      ): Parameter[RecordB] =
        new Parameter[RecordB] {
          type Field = _Field
          type Value = _Value

          def field: RecordB => Field = _field

          def newValue: Value = _newValue

          def shape: slick.lifted.Shape[_ <: FlatShapeLevel, Field, Value, Field] = _shape
        }
    }

    implicit class ParameterizedQuery[RecordC, U, C[_], A, F](val underlying: Query[RecordC, U, C]) {
      def insertWithParameters(parameters: Seq[Parameter[RecordC]]): FixedSqlAction[U, NoStream, Effect.Write] =
        parameters.reduceLeft(_ and _).insert(underlying)

      def updateReturningWithParameters(returningQuery: Query[A, F, C], parameters: Seq[Parameter[RecordC]]): SqlStreamingAction[Vector[F], F, Effect] =
        parameters.reduceLeft(_ and _).updateReturning(underlying, returningQuery)

      def updateWithParameters(parameters: Seq[Parameter[RecordC]]): FixedSqlAction[Int, NoStream, Effect.Write] =
        parameters.reduceLeft(_ and _).update(underlying)
    }

    implicit class UpdateReturningInvoker[E, U, C[_]](updateQuery: Query[E, U, C]) {
      // scalastyle:off cyclomatic.complexity
      def updateReturning[A, F](returningQuery: Query[A, F, C], v: U): SqlStreamingAction[Vector[F], F, Effect] = {
        val ResultSetMapping(_, CompiledStatement(_, sres: SQLBuilder.Result, _), CompiledMapping(_, _)) =
          PostgresProfile.updateCompiler.run(updateQuery.toNode).tree: @unchecked

        val returningNode = returningQuery.toNode
        val fieldNames = returningNode match {
          case Bind(_, _, Pure(Select(_, col), _)) =>
            List(col.name)
          case Bind(_, _, Pure(ProductNode(children), _)) =>
            children.map {
              case Select(_, col) => col.name
              case _ => throw new NotImplementedError()
            }.toSeq
          case Bind(_, _, TableExpansion(_, _, TypeMapping(ProductNode(children), _, _))) =>
            children.map {
              case Select(_, col) => col.name
              case _ => throw new NotImplementedError()
            }.toSeq
          case Pure(Select(_, col), _) =>
            List(col.name)
          case Pure(ProductNode(children), _) =>
            children.map {
              case Select(_, col) => col.name
              case _ => throw new NotImplementedError()
            }.toSeq
          case TableExpansion(_, _, typeMapping: TypeMapping) =>
            extractFieldNamesFromTypeMapping(typeMapping)

          case _ => throw new NotImplementedError()
        }

        implicit val pconv: SetParameter[U] = {
          val ResultSetMapping(_, _, CompiledMapping(converter, _)) =
            PostgresProfile.updateCompiler.run(updateQuery.toNode).tree: @unchecked
          SetParameter[U] { (value, params) =>
            converter.asInstanceOf[ResultConverter[JdbcResultConverterDomain, U]].set(value, params.ps)
          }
        }

        implicit val rconv: GetResult[F] = {
          val ResultSetMapping(_, _, CompiledMapping(converter, _)) =
            PostgresProfile.queryCompiler.run(returningNode).tree: @unchecked
          GetResult[F] { p => converter.asInstanceOf[ResultConverter[JdbcResultConverterDomain, F]].read(p.rs) }
        }

        val fieldsExp = fieldNames.map(PostgresProfile.quoteIdentifier).mkString(", ")
        val pconvUnit = pconv.applied(v)
        val sql = sres.sql + s" RETURNING $fieldsExp"

        SQLActionBuilder(List(sql), pconvUnit).as[F]
      }
    }

    private val extractFieldNamesFromTypeMapping: PartialFunction[TypeMapping, Seq[String]] = { case TypeMapping(ProductNode(children), _, _) =>
      children.toSeq.flatMap {
        case Select(_, col) => Seq(col.name)
        case OptionApply(Select(_, childCol)) => Seq(childCol.name)
        case typeMapping: TypeMapping => extractFieldNamesFromTypeMapping(typeMapping)
        case _ => throw new NotImplementedError()
      }
    }
    // scalastyle:on

    // format: on

    // https://github.com/slick/slick/issues/2249 ( .filter(_.x === None) doesn't generate a "x is null" query )
    implicit class RepExtensions[T](val rep: Rep[Option[T]]) {
      def is(targetValue: Option[T])(implicit slickRecognizedType: BaseTypedType[T]): Rep[Option[Boolean]] =
        targetValue match {
          case Some(value) => rep === value
          case None => rep.isEmpty.?
        }
    }

  }
}

object PostgresProfile extends PostgresProfile
