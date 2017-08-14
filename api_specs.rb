module Xplocity
  class API < Grape::API
    version 'v1'
    format :xml
    formatter :xml, lambda { |object, env| object.to_xml(:skip_types => true, :dasherize => false) }
    prefix :api



    resource :locations do

      desc 'Get list of random locations.'
      params do
        requires :loc_count, type: Integer, desc: 'Location count'
        requires :optimal_distance, type: Float, desc: 'Desired distance between locations (km)'
        requires :latitude, type: Float, desc: 'User latitude'
        requires :longitude, type: Float, desc: 'User longitude'

        #optional :category[], type: Integer, desc: 'Array of location categories ids'

      end
      get :get_location_list do
        #Location.where("region_id is not NULL").first(params[:loc_count])
        if params[:category] == nil
          lm = LocationManager.new(params[:optimal_distance], params[:loc_count], nil, {:enabled => true, :user => 1})
        else
          lm = LocationManager.new(params[:optimal_distance], params[:loc_count], params[:category], {:enabled => true, :user => 1})
        end

        lm.get_locations(params[:latitude], params[:longitude])
        Location.where("id IN (?)", lm.locations)
      end

    end


    resource :location_categories do
      desc 'Get all location categories.'
      get do
        LocationCategory.all
      end
    end


    resource :regions do
      desc 'Get all regions.'
      get do
        Region.all
      end
    end


    resource :chains do

      desc 'User chain list'
      params do
        requires :user_id, type: Integer, desc: 'User id'
      end
      get do
        chains = User.find(params[:user_id]).chains

        builder = Nokogiri::XML::Builder.new do |xml|
          xml.User{
            chains.each do |c|
              xml.Chain(:id => c.id){
                xml.date c.created_at
                xml.Locations {
                  xml.total c.locations.count
                  xml.explored c.chain_locations.where(:explored => 1).count
                }

              }
            end
          }
        end
        return builder
      end

      desc 'Get chain'
      params do
        requires :id, type: Integer, desc: 'Chain id'
      end
      get ':id' do
        c = Chain.find(params[:id])
      end

      desc 'New chain'
      params do
        requires :api_token, type: String, desc: 'Authentication token'
      end
      post do
        doc = Nokogiri::XML(request.body.read)
        c = Chain.new()
        c.user = User.find(params[:api_token])
        c.route = doc.xpath("//Chain/Route").text
        c.save()

        #regions
        doc.xpath("//Chain/Regions/Region").each do |r|
          reg = Region.find(r.xpath("@id").first.value.to_i())
          if reg != nil

            c.regions << reg
            chain_reg = c.chain_regions.find_by_region_id(reg.id)
            chain_reg.explored = r.xpath("./explored").text.to_i()
            chain_reg.save
          end
        end

        #locations
        doc.xpath("//Chain/Locations/Location").each do |r|
          loc = Location.find(r.xpath("@id").first.value.to_i())
          if loc != nil
            c.locations << loc
            chain_loc = c.chain_locations.find_by_location_id(loc.id)
            chain_loc.explored = r.xpath("./explored").text.to_i()
            chain_loc.save
          end
        end

        return {:status => 'success'}
      end


      desc 'Delete chain'
      params do
        requires :id, type: Integer, desc: 'Chain id'
      end
      delete do

        begin
          Chain.find(params[:id]).destroy
        rescue ActiveRecord::RecordNotFound => e
          return {:status => 'not found'}
        end

        return {:status => 'success'}
      end



    end

  end
end